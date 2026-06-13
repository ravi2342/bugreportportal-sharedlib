// vars/gitCheckout.groovy
// Git checkout into subdirectory

def call(Map config) {
    String branch = config.branch ?: 'master'
    String repoUrl = config.repoUrl ?: ''
    String targetDir = config.targetDir ?: 'repo'
    
    try {
        echo "=== Checking out ${targetDir} ==="
        echo "Repository: ${repoUrl}"
        echo "Branch: ${branch}"
        
        sh """
            set -e
            git clone --branch ${branch} ${repoUrl} ${targetDir}
            echo "✓ Cloned to: ${targetDir}"
        """
        
        return true
    } catch (Exception e) {
        echo "❌ Git checkout failed: ${e.message}"
        throw e
    }
}
