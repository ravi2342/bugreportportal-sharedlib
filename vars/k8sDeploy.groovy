// vars/k8sDeploy.groovy
// Deploy application to Kubernetes

def call(Map config) {
    String imageTag = config.imageTag ?: ''
    String clusterContext = config.clusterContext ?: 'kind-bug-report-portal'
    String namespace = config.namespace ?: 'bug-report-portal'
    String deploymentName = config.deploymentName ?: 'bug-report-portal-app'
    // Image name placeholder declared in kustomization.yaml's `images:` block.
    // Distinct from the Deployment resource name — must be provided explicitly.
    String imageName = config.imageName ?: ''
    boolean skipTlsVerify = config.skipTlsVerify != null ? config.skipTlsVerify : true
    String k8sManifestDir = config.manifestDir ?: 'devops/k8s'
    
    if (!imageTag) {
        error("imageTag is required for Kubernetes deployment")
    }
    if (!imageName) {
        error("imageName is required for Kubernetes deployment (the image placeholder in kustomization.yaml's `images:` block)")
    }
    
    try {
        echo "=== Deploying to Kubernetes ==="
        
        def kubectlAvailable = sh(
            script: "command -v kubectl >/dev/null 2>&1",
            returnStatus: true
        ) == 0
        
        if (!kubectlAvailable) {
            error("kubectl not found on agent")
        }
        
        sh """
            set -e
            
            echo "Creating temporary kubeconfig for Jenkins container..."
            TEMP_KUBECONFIG="/tmp/kubeconfig-jenkins-${BUILD_NUMBER}"
            cp ~/.kube/config "\$TEMP_KUBECONFIG"
            export KUBECONFIG="\$TEMP_KUBECONFIG"
            echo "✓ Using temporary kubeconfig: \$TEMP_KUBECONFIG"
            echo "✓ Original ~/.kube/config will remain unchanged"
            
            echo "Setting kubectl context to cluster..."
            kubectl config use-context ${clusterContext}
            
            echo "Extracting cluster server endpoint from kubeconfig..."
            KUBE_SERVER=\$(kubectl config view -o jsonpath='{.clusters[?(@.name=="${clusterContext}")].cluster.server}')
            echo "Original kubeconfig server: \$KUBE_SERVER"
            
            if [ -z "\$KUBE_SERVER" ]; then
                echo "❌ ERROR: Could not extract cluster server from kubeconfig"
                exit 1
            fi
            
            echo "Adjusting server address for Docker container access..."
            KUBE_SERVER=\$(echo "\$KUBE_SERVER" | sed 's|127.0.0.1|host.docker.internal|g')
            echo "Adjusted server for container: \$KUBE_SERVER"
            
            echo "Updating temporary kubeconfig with container-compatible address..."
            kubectl config set-cluster ${clusterContext} --server="\$KUBE_SERVER" || true
            echo "✓ Temporary kubeconfig updated"
            
            echo "Checking cluster connectivity..."
            kubectl ${skipTlsVerify ? '--insecure-skip-tls-verify' : ''} cluster-info
            
            echo "Navigating to k8s manifests directory..."
            cd ${k8sManifestDir}
            
            echo "Setting Docker image tag: ${imageTag}"
            kustomize edit set image ${imageName}=${imageTag}
            
            echo "Applying Kubernetes manifests with dynamic image..."
            kubectl ${skipTlsVerify ? '--insecure-skip-tls-verify' : ''} apply -k .
            
            echo "Waiting for rollout..."
            kubectl ${skipTlsVerify ? '--insecure-skip-tls-verify' : ''} rollout status deployment/${deploymentName} -n ${namespace} --timeout=120s
            
            echo "✓ Kubernetes deployment successful"
            echo ""
            echo "Deployment Summary:"
            kubectl ${skipTlsVerify ? '--insecure-skip-tls-verify' : ''} get deployment ${deploymentName} -n ${namespace}
            kubectl ${skipTlsVerify ? '--insecure-skip-tls-verify' : ''} get pods -n ${namespace}
        """
    } catch (Exception e) {
        error("Kubernetes deployment failed: ${e.message}")
    }
}
