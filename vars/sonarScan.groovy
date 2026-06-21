// vars/sonarScan.groovy
// SonarQube/SonarCloud analysis

def call(Map config) {
    String hostUrl = config.hostUrl ?: 'http://sonarqube:9000'
    String projectKey = config.projectKey ?: 'bug-report-portal'
    String tokenCredId = config.tokenCredId ?: 'sonar-token'
    boolean waitForQualityGate = config.waitForQualityGate != null ? config.waitForQualityGate : true
    // Directory holding sonar-project.properties (now lives in the app repo)
    String workDir = config.workDir ?: 'app'
    // projectBaseDir is interpreted relative to workDir; '.' = analyze the workDir itself
    String projectBaseDir = config.projectBaseDir ?: '.'
    // Extra `-Dkey=value` flags appended to the sonar-scanner invocation (e.g. PR decoration args)
    List extraArgs = (config.extraArgs instanceof List) ? config.extraArgs : []
    String extraArgsStr = extraArgs.join(' ')
    
    try {
        echo "=== Running SonarQube analysis ==="
        
        withCredentials([string(credentialsId: tokenCredId, variable: 'SONAR_TOKEN')]) {
            def sonarAvailable = sh(
                script: "command -v sonar-scanner >/dev/null 2>&1",
                returnStatus: true
            ) == 0
            
            if (sonarAvailable) {
                sh """
                    set -e
                    echo "Starting SonarQube analysis from ${workDir} directory..."
                    cd ${workDir}
                    
                    echo "Reading configuration from sonar-project.properties..."
                    cat sonar-project.properties
                    
                    echo ""
                    echo "Running sonar-scanner against SonarQube..."
                    sonar-scanner \\
                        -Dproject.settings=sonar-project.properties \\
                        -Dsonar.projectBaseDir=${projectBaseDir} \\
                        -Dsonar.host.url="${hostUrl}" \\
                        -Dsonar.projectKey="${projectKey}" \\
                        -Dsonar.token="\${SONAR_TOKEN}" \\
                        ${waitForQualityGate ? '-Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=300' : ''} \\
                        ${extraArgsStr}
                    
                    echo ""
                    echo "✓ Analysis complete"
                    echo "View results at: ${hostUrl}/dashboard?id=${projectKey}"
                """
            } else {
                echo "⚠ sonar-scanner not installed - SonarQube scan skipped"
            }
        }
    } catch (Exception e) {
        error("SonarQube scan failed: ${e.message}")
    }
}
