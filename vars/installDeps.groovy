// vars/installDeps.groovy
// Install npm dependencies

def call() {
    try {
        echo "=== Installing dependencies ==="
        sh 'cd app && npm ci'
        echo "✓ Dependencies installed"
    } catch (Exception e) {
        error("Dependency installation failed: ${e.message}")
    }
}
