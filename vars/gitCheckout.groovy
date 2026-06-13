// vars/gitCheckout.groovy
// Git checkout and repository initialization

def call(Map config) {
    String branch = config.branch ?: 'master'
    String repoUrl = config.repoUrl ?: ''
    
    node {
        try {
            echo "=== Checking out repository ==="
            echo "Repository: ${repoUrl}"
            echo "Branch: ${branch}"
            
            checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${branch}"]],
                userRemoteConfigs: [[url: repoUrl]],
                poll: false
            ])
            
            sh '''
                set -e
                echo "✓ Repository checked out"
                echo "Workspace structure:"
                ls -la
            '''
            
            return true
        } catch (Exception e) {
            echo "❌ Git checkout failed: ${e.message}"
            throw e
        }
    }
}
