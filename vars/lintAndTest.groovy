// vars/lintAndTest.groovy
// Run linting and tests with coverage
// Includes timeouts to prevent hanging

def call(Map config = [:]) {
    String workDir = config.workDir ?: 'app'
    try {
        // Lint Stage
        echo "=== Running lint (workDir: ${workDir}) ==="
        def hasLint = sh(
            script: "node -e \"const p=require('./${workDir}/package.json'); process.exit((p.scripts && p.scripts.lint) ? 0 : 1)\"",
            returnStatus: true
        ) == 0
        
        if (hasLint) {
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    sh "cd ${workDir} && npm run lint"
                }
                echo "✓ Lint passed"
            } catch (Exception e) {
                echo "❌ Lint timed out or failed after 15 minutes: ${e.message}"
                error("Lint stage exceeded 15-minute timeout - code too large or ESLint too slow")
            }
        } else {
            echo "⊘ No lint script configured - skipping"
        }
        
        // Test Stage
        echo "=== Running tests with coverage ==="
        def hasTests = sh(
            script: "node -e \"const p=require('./${workDir}/package.json'); process.exit((p.scripts && p.scripts.test && !p.scripts.test.includes('no test')) ? 0 : 1)\"",
            returnStatus: true
        ) == 0
        
        if (hasTests) {
            try {
                timeout(time: 15, unit: 'MINUTES') {
                    sh """
                        set -e
                        cd ${workDir}
                        npm test -- --coverage --coverageReporters=lcov --coverageReporters=text --coverageReporters=text-summary
                    """
                }
                echo "✓ Tests passed with coverage report at ${workDir}/coverage/lcov.info"
            } catch (Exception e) {
                echo "❌ Tests timed out or failed after 15 minutes: ${e.message}"
                error("Test stage exceeded 15-minute timeout - tests taking too long")
            }
        } else {
            echo "⊘ No test script configured - skipping"
        }
    } catch (Exception e) {
        error("Lint/Test failed: ${e.message}")
    }
}
