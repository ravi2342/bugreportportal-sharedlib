// vars/approveDeployment.groovy
// Handle deployment approval with proper logging

def call(Map config) {
    String environment = config.environment ?: 'DEV'
    Integer timeoutMins = config.timeoutMins ?: 30
    String buildNumber = config.buildNumber ?: env.BUILD_NUMBER
    
    try {
        timeout(time: timeoutMins, unit: 'MINUTES') {
            input message: "Approve deployment to ${environment} environment?",
                ok: "✓ Proceed with ${environment}",
                submitter: null
            
            // Jenkins automatically logs "Approved by USERNAME" above this message
            // Add visual confirmation of proceeding with deployment
            echo ""
            echo "═══════════════════════════════════════════════════════════"
            echo "✓ APPROVAL CONFIRMED"
            echo "✓ BUILD NUMBER: #${buildNumber}"
            echo "✓ TARGET ENVIRONMENT: ${environment}"
            echo "✓ PROCEEDING WITH DEPLOYMENT..."
            echo "═══════════════════════════════════════════════════════════"
            echo ""
        }
    } catch (Exception e) {
        error("❌ Deployment rejected or approval timed out (${timeoutMins} min expired)")
    }
}

