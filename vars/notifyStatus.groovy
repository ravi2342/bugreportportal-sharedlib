// vars/notifyStatus.groovy
// Notify build status via Slack/email

def call(Map config) {
    String buildStatus = config.buildStatus ?: 'SUCCESS'
    String buildNumber = config.buildNumber ?: env.BUILD_NUMBER
    String jobName = config.jobName ?: env.JOB_NAME
    String imageTag = config.imageTag ?: ''
    boolean deployed = config.deployed != null ? config.deployed : false
    
    String statusIcon = buildStatus == 'SUCCESS' ? '✓' : '✗'
    String statusColor = buildStatus == 'SUCCESS' ? 'good' : 'danger'
    
    try {
        echo """
        ╔═══════════════════════════════════════════════════════════════╗
        ║                     BUILD COMPLETE                            ║
        ╠═══════════════════════════════════════════════════════════════╣
        ║ Status:          ${statusIcon} ${buildStatus}
        ║ Build:           #${buildNumber}
        ║ Job:             ${jobName}
        ║ Image:           ${imageTag ?: 'N/A'}
        ║ Deployed:        ${deployed ? '✓ Yes' : '✗ No'}
        ║ Timestamp:       \$(date)
        ╚═══════════════════════════════════════════════════════════════╝
        """
        
        // Optional: Slack notification
        // Uncomment and configure webhook URL if needed
        /*
        if (env.SLACK_WEBHOOK_URL?.trim()) {
            def slackMessage = [
                attachments: [
                    [
                        color: statusColor,
                        title: "Build #${buildNumber} ${buildStatus}",
                        text: jobName,
                        fields: [
                            [title: "Status", value: buildStatus, short: true],
                            [title: "Image", value: imageTag ?: 'N/A', short: true],
                            [title: "Deployed", value: deployed ? 'Yes' : 'No', short: true]
                        ]
                    ]
                ]
            ]
            sh """
                curl -X POST -H 'Content-type: application/json' \\
                    --data '${slackMessage.toString()}' \\
                    ${env.SLACK_WEBHOOK_URL}
            """
        }
        */
    } catch (Exception e) {
        echo "⚠ Notification failed: ${e.message}"
    }
}
