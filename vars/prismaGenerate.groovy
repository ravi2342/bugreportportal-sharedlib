// vars/prismaGenerate.groovy
// Generate Prisma client and schema

def call() {
    try {
        echo "=== Running Prisma generate ==="
        sh '''
            set -e
            cd app
            
            # Copy .env.docker.example to .env if .env doesn't exist
            if [ ! -f .env ]; then
                if [ -f .env.docker.example ]; then
                    cp .env.docker.example .env
                    echo "Created .env from .env.docker.example"
                else
                    echo "❌ ERROR: Neither .env nor .env.docker.example found"
                    exit 1
                fi
            fi
            
            npx prisma generate
        '''
        echo "✓ Prisma schema generated"
    } catch (Exception e) {
        error("Prisma generation failed: ${e.message}")
    }
}
