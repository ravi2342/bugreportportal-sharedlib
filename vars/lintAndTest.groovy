// vars/lintAndTest.groovy
// Run linting and tests with coverage

def call() {
    try {
        // Lint Stage
        echo "=== Running lint ==="
        def hasLint = sh(
            script: "node -e \"const p=require('./app/package.json'); process.exit((p.scripts && p.scripts.lint) ? 0 : 1)\"",
            returnStatus: true
        ) == 0
        
        if (hasLint) {
            sh 'cd app && npm run lint'
            echo "✓ Lint passed"
        } else {
            echo "⊘ No lint script configured - skipping"
        }
        
        // Test Stage
        echo "=== Running tests with coverage ==="
        def hasTests = sh(
            script: "node -e \"const p=require('./app/package.json'); process.exit((p.scripts && p.scripts.test && !p.scripts.test.includes('no test')) ? 0 : 1)\"",
            returnStatus: true
        ) == 0
        
        if (hasTests) {
            sh '''
                set -e
                cd app
                npm test -- --coverage --coverageReporters=lcov --coverageReporters=text --coverageReporters=text-summary
            '''
            echo "✓ Tests passed with coverage report at app/coverage/lcov.info"
        } else {
            echo "⊘ No test script configured - skipping"
        }
    } catch (Exception e) {
        error("Lint/Test failed: ${e.message}")
    }
}
