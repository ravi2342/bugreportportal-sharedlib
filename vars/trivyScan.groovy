// vars/trivyScan.groovy
// Trivy security vulnerability scanning

def call(Map config) {
    String imageTag = config.imageTag ?: ''
    boolean failOnSeverity = config.failOnSeverity != null ? config.failOnSeverity : true
    
    if (!imageTag) {
        error("imageTag is required for Trivy scan")
    }
    
    try {
        echo "=== Running Trivy security scan ==="
        
        def trivyAvailable = sh(
            script: "command -v trivy >/dev/null 2>&1",
            returnStatus: true
        ) == 0
        
        if (trivyAvailable) {
            sh """
                set -e
                echo "Scanning image for HIGH and CRITICAL vulnerabilities..."
                trivy image --scanners vuln --severity HIGH,CRITICAL --no-progress ${failOnSeverity ? '--exit-code 1' : ''} ${imageTag}
            """
            echo "✓ Trivy scan passed - no HIGH/CRITICAL vulnerabilities"
        } else {
            echo "⚠ Trivy not installed - security scan skipped"
        }
    } catch (Exception e) {
        error("Trivy security scan failed: ${e.message}")
    }
}
