// vars/dockerBuild.groovy
// Build Docker image

def call(Map config) {
    String imageTag = config.imageTag ?: 'bug-report-portal:latest'
    String dockerfile = config.dockerfile ?: 'app'
    
    try {
        echo "=== Building Docker image: ${imageTag} ==="
        sh "docker build -t ${imageTag} ${dockerfile}"
        echo "✓ Docker image built successfully"
    } catch (Exception e) {
        error("Docker build failed: ${e.message}")
    }
}
