// vars/preflightChecks.groovy
// Verify required tools are available

def call() {
    try {
        echo "=== Running preflight checks ==="
        sh '''
            set -e
            echo "Checking required tools..."
            echo "Node: $(node -v)"
            echo "npm: $(npm -v)"
            echo "Docker: $(docker --version)"
            
            # Check for required tools
            if ! command -v docker >/dev/null 2>&1; then
                echo "❌ ERROR: docker not found"
                exit 1
            fi
            
            if ! command -v trivy >/dev/null 2>&1; then
                echo "⚠ WARNING: trivy not found - security scan will be skipped"
            fi
            
            if ! command -v sonar-scanner >/dev/null 2>&1; then
                echo "⚠ WARNING: sonar-scanner not found - SonarQube scan will be skipped"
            fi
            
            echo "✓ All critical tools available"
        '''
    } catch (Exception e) {
        error("Preflight checks failed: ${e.message}")
    }
}
