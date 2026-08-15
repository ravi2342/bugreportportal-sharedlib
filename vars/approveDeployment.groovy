// vars/approveDeployment.groovy
// Handle deployment approval with approver name capture

def call(Map config) {
    String environment = config.environment ?: 'DEV'
    Integer timeoutMins = config.timeoutMins ?: 30
    String buildNumber = config.buildNumber ?: env.BUILD_NUMBER
    
    try {
        timeout(time: timeoutMins, unit: 'MINUTES') {
            // Use BuildUser wrapper to capture who approves
            wrap([$class: 'BuildUser']) {
                def approver = env.BUILD_USER ?: env.BUILD_USER_ID ?: 'Unknown User'
                
                // Wait for approval
                input message: "Approve deployment to ${environment} environment?",
                    ok: "✓ Proceed with ${environment}",
                    submitter: null
                
                // Log approval with captured user
                echo ""
                echo "═══════════════════════════════════════════════════════════"
                echo "✓ DEPLOYMENT APPROVED BY: ${approver.toUpperCase()}"
                echo "✓ BUILD NUMBER: #${buildNumber}"
                echo "✓ TARGET ENVIRONMENT: ${environment}"
                echo "✓ PROCEEDING WITH DEPLOYMENT..."
                echo "═══════════════════════════════════════════════════════════"
                echo ""
            }
        }
    } catch (Exception e) {
        error("❌ Deployment rejected or approval timed out (${timeoutMins} min expired)")
    }
}
