# Jenkins Shared Library - Bug Report Portal

Reusable Jenkins pipeline functions for the Bug Report Portal CI/CD pipeline.

## Overview

This library abstracts common pipeline operations into reusable groovy functions, making Jenkinsfiles cleaner and more maintainable.

## Structure

```
bugreportportal-sharedlib/
├── vars/                          # Global variables (entry points)
│   ├── gitCheckout.groovy         # Git repository checkout
│   ├── preflightChecks.groovy     # Verify required tools
│   ├── installDeps.groovy         # npm install
│   ├── prismaGenerate.groovy      # Prisma client generation
│   ├── lintAndTest.groovy         # Lint and run tests with coverage
│   ├── sonarScan.groovy           # SonarQube/SonarCloud analysis
│   ├── dockerBuild.groovy         # Build Docker image
│   ├── trivyScan.groovy           # Trivy security scanning
│   ├── dockerPush.groovy          # Push image to registry
│   ├── k8sDeploy.groovy           # Deploy to Kubernetes
│   └── notifyStatus.groovy        # Send build notifications
├── test/                          # Test scaffolding
└── README.md                      # This file
```

## Installation

### 1. Add to Jenkins

**Option A: Jenkins UI**
1. Go to **Manage Jenkins** → **System** → **Global Pipeline Libraries**
2. Click **Add**
3. Set:
   - **Name:** `bug-report-portal-lib`
   - **Default version:** `master`
   - **Repository URL:** `https://github.com/ravi2342/bugreportportal-sharedlib.git`
4. Click **Save**

**Option B: Jenkins Configuration as Code (JCasC)**
```yaml
unclassified:
  location:
    pipelines:
      libraries:
        - name: bug-report-portal-lib
          version: master
          scope: GLOBAL
          defaultVersion: master
          retriever:
            gitSCM:
              remote: https://github.com/ravi2342/bugreportportal-sharedlib.git
```

### 2. Use in Jenkinsfile

```groovy
@Library('bug-report-portal-lib') _

pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                gitCheckout(
                    branch: params.BRANCH,
                    repoUrl: params.GITHUB_REPO_URL
                )
            }
        }
        
        stage('Preflight') {
            steps {
                preflightChecks()
            }
        }
        
        stage('Install') {
            steps {
                installDeps()
                prismaGenerate()
            }
        }
        
        stage('Quality') {
            steps {
                lintAndTest()
            }
        }
        
        stage('SonarQube') {
            when { expression { params.RUN_SONAR } }
            steps {
                sonarScan(
                    hostUrl: params.SONAR_HOST_URL,
                    projectKey: params.SONAR_PROJECT_KEY,
                    tokenCredId: params.SONAR_TOKEN_CREDENTIALS_ID
                )
            }
        }
        
        stage('Build') {
            steps {
                dockerBuild(imageTag: env.IMAGE_TAG)
            }
        }
        
        stage('Security') {
            steps {
                trivyScan(imageTag: env.IMAGE_TAG)
            }
        }
        
        stage('Push') {
            when { expression { params.DO_PUSH } }
            steps {
                dockerPush(
                    imageTag: env.IMAGE_TAG,
                    registryCredId: params.REGISTRY_CREDENTIALS_ID
                )
            }
        }
        
        stage('Deploy') {
            when { expression { params.DO_DEPLOY } }
            steps {
                k8sDeploy(
                    imageTag: env.IMAGE_TAG,
                    clusterContext: 'kind-bug-report-portal',
                    namespace: 'bug-report-portal'
                )
            }
        }
        
        stage('Notify') {
            steps {
                notifyStatus(
                    buildStatus: currentBuild.result,
                    buildNumber: env.BUILD_NUMBER,
                    jobName: env.JOB_NAME,
                    imageTag: env.IMAGE_TAG,
                    deployed: params.DO_DEPLOY
                )
            }
        }
    }
}
```

## Function Reference

### gitCheckout(Map config)
Clones the application repository into a subdirectory.

**Parameters:**
- `branch` (String): Git branch to check out (default: `'master'`)
- `repoUrl` (String): Repository URL (required)
- `targetDir` (String): Subdirectory to clone into (default: `'repo'`). Must not already exist, or `git clone` will fail.

**Example:**
```groovy
gitCheckout(
    branch: 'master',
    repoUrl: 'https://github.com/ravi2342/bugreportportal.git',
    targetDir: 'app'
)
```

---

### preflightChecks()
Verifies all required tools are available

**Example:**
```groovy
preflightChecks()
```

---

### installDeps()
Installs npm dependencies using `npm ci`

**Example:**
```groovy
installDeps()
```

---

### prismaGenerate()
Generates Prisma client and schema

**Example:**
```groovy
prismaGenerate()
```

---

### lintAndTest()
Runs ESLint and Jest tests with coverage

**Example:**
```groovy
lintAndTest()
```

---

### sonarScan(Map config)
Runs SonarQube/SonarCloud analysis

**Parameters:**
- `hostUrl` (String): SonarQube server URL (default: 'http://sonarqube:9000')
- `projectKey` (String): Project key in SonarQube (default: 'bug-report-portal')
- `tokenCredId` (String): Jenkins credentials ID for token (default: 'sonar-token')
- `waitForQualityGate` (Boolean): Wait for quality gate result (default: true)

**Example:**
```groovy
sonarScan(
    hostUrl: 'https://sonarcloud.io',
    projectKey: 'ravi2342_bugreportportal',
    tokenCredId: 'sonarcloud-token',
    waitForQualityGate: true
)
```

---

### dockerBuild(Map config)
Builds Docker image

**Parameters:**
- `imageTag` (String): Full image tag, e.g. `'myregistry/app:1.0-123'` (default: `'bug-report-portal:latest'`)
- `dockerfile` (String): Path to build context directory (default: `'app'`)

**Example:**
```groovy
dockerBuild(
    imageTag: 'docker.io/ravi2342/bugreportportal:1.0-123',
    dockerfile: 'app'
)
```

---

### trivyScan(Map config)
Runs Trivy security scanning

**Parameters:**
- `imageTag` (String): Docker image tag to scan (required)
- `failOnSeverity` (Boolean): Fail build on HIGH/CRITICAL (default: true)

**Example:**
```groovy
trivyScan(
    imageTag: 'docker.io/ravi2342/bugreportportal:1.0-123',
    failOnSeverity: true
)
```

---

### dockerPush(Map config)
Pushes Docker image to registry

**Parameters:**
- `imageTag` (String): Full image tag to push (required)
- `registryCredId` (String): Jenkins credentials ID for registry (default: 'dockerhub-creds-pat')

**Example:**
```groovy
dockerPush(
    imageTag: 'docker.io/ravi2342/bugreportportal:1.0-123',
    registryCredId: 'dockerhub-creds-pat'
)
```

---

### k8sDeploy(Map config)
Deploys application to Kubernetes cluster

**Parameters:**
- `imageTag` (String): Docker image tag to deploy (required)
- `clusterContext` (String): kubectl context name (default: 'kind-bug-report-portal')
- `namespace` (String): Kubernetes namespace (default: 'bug-report-portal')
- `deploymentName` (String): Deployment name (default: 'bug-report-portal-app')
- `skipTlsVerify` (Boolean): Skip TLS verification (default: true)
- `manifestDir` (String): Path to k8s manifests (default: 'devops/k8s')

**Example:**
```groovy
k8sDeploy(
    imageTag: 'docker.io/ravi2342/bugreportportal:1.0-123',
    clusterContext: 'kind-bug-report-portal',
    namespace: 'bug-report-portal',
    deploymentName: 'bug-report-portal-app'
)
```

---

### notifyStatus(Map config)
Sends build status notifications

**Parameters:**
- `buildStatus` (String): Build status, e.g. `'SUCCESS'` or `'FAILED'` (default: `'SUCCESS'`)
- `buildNumber` (String): Build number (default: `env.BUILD_NUMBER`)
- `jobName` (String): Job name (default: `env.JOB_NAME`)
- `imageTag` (String): Docker image tag deployed (default: `''`)
- `deployed` (Boolean): Whether deployment occurred (default: `false`)

**Example:**
```groovy
notifyStatus(
    buildStatus: currentBuild.result,
    buildNumber: env.BUILD_NUMBER,
    jobName: env.JOB_NAME,
    imageTag: 'docker.io/ravi2342/bugreportportal:1.0-123',
    deployed: true
)
```

---

## Usage Patterns

### Basic CI/CD
```groovy
@Library('bug-report-portal-lib') _

pipeline {
    agent any
    
    stages {
        stage('Checkout') { steps { gitCheckout(branch: 'master', repoUrl: env.REPO_URL) } }
        stage('Install') { steps { installDeps(); prismaGenerate() } }
        stage('Quality') { steps { lintAndTest() } }
        stage('Build') { steps { dockerBuild(imageTag: env.IMAGE_TAG) } }
        stage('Security') { steps { trivyScan(imageTag: env.IMAGE_TAG) } }
    }
}
```

### Full Pipeline with Deploy
```groovy
@Library('bug-report-portal-lib') _

pipeline {
    agent any
    parameters {
        string(name: 'BRANCH', defaultValue: 'master')
        booleanParam(name: 'DO_PUSH', defaultValue: false)
        booleanParam(name: 'DO_DEPLOY', defaultValue: false)
    }
    
    stages {
        stage('Checkout') { steps { gitCheckout(branch: params.BRANCH, repoUrl: env.REPO_URL) } }
        stage('Setup') { steps { preflightChecks(); installDeps(); prismaGenerate() } }
        stage('Quality') { steps { lintAndTest() } }
        stage('Build') { steps { dockerBuild(imageTag: env.IMAGE_TAG) } }
        stage('Security') { steps { trivyScan(imageTag: env.IMAGE_TAG) } }
        stage('Push') {
            when { expression { params.DO_PUSH } }
            steps { dockerPush(imageTag: env.IMAGE_TAG) }
        }
        stage('Deploy') {
            when { expression { params.DO_DEPLOY } }
            steps { k8sDeploy(imageTag: env.IMAGE_TAG) }
        }
    }
    post { always { notifyStatus(buildStatus: currentBuild.result) } }
}
```

---

## Troubleshooting

### Library not found
- Verify library is configured in Jenkins > Manage Jenkins > System > Global Pipeline Libraries
- Check library name matches `@Library('bug-report-portal-lib')`
- Ensure repository is accessible from Jenkins

### Function not available
- Make sure function file exists in `vars/` directory (e.g., `vars/sonarScan.groovy`)
- Function names match file names (without .groovy extension)
- Reload Jenkins configuration: Manage Jenkins > Reload Configuration

### Groovy compilation errors
- Check syntax in groovy files
- Use `def` for variable declarations
- Use `sh` for shell commands within `node` or `agent` blocks

---

## Contributing

1. Add new functions to `vars/` directory
2. Name file as `functionName.groovy`
3. Use descriptive parameter names
4. Add comments explaining function purpose
5. Update README with function documentation
6. Test in a non-production Jenkins instance first

---

## License

Same as Bug Report Portal project
