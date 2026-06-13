// vars/dockerPush.groovy
// Push Docker image to registry

def call(Map config) {
    String imageTag = config.imageTag ?: ''
    String registryCredId = config.registryCredId ?: 'dockerhub-creds-pat'
    
    if (!imageTag) {
        error("imageTag is required for Docker push")
    }
    
    try {
        echo "=== Pushing Docker image to registry ==="
        
        if (registryCredId?.trim()) {
            withCredentials([usernamePassword(credentialsId: registryCredId, usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
                sh """
                    set -e
                    echo "Logging in to Docker Hub..."
                    echo "\${REG_PASS}" | docker login -u "\${REG_USER}" --password-stdin
                    
                    echo "Pushing image: ${imageTag}"
                    docker push ${imageTag}
                    
                    docker logout
                    echo "✓ Image pushed successfully"
                """
            }
        } else {
            sh """
                set -e
                echo "Pushing image: ${imageTag}"
                docker push ${imageTag}
                echo "✓ Image pushed successfully"
            """
        }
    } catch (Exception e) {
        error("Docker push failed: ${e.message}")
    }
}
