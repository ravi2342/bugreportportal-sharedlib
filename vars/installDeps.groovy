// vars/installDeps.groovy
// Install npm dependencies

def call(Map config = [:]) {
    String workDir = config.workDir ?: 'app'
    try {
        echo "=== Installing dependencies (workDir: ${workDir}) ==="
        sh "cd ${workDir} && npm ci"
        echo "✓ Dependencies installed"
    } catch (Exception e) {
        error("Dependency installation failed: ${e.message}")
    }
}
