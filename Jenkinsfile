// ==============================================================================
// QueueLess — CI/CD Pipeline (Jenkinsfile)
// College DevOps Laboratory Project — 23IT723 (Experiments 4, 5, 9)
// Declarative Pipeline with Automated Testing, Docker Build & Container Deployment
// ==============================================================================

pipeline {
    agent any

    parameters {
        string(name: 'APP_PORT', defaultValue: '8085', description: 'Host port to bind the QueueLess container')
        string(name: 'CONTAINER_NAME', defaultValue: 'queueless-app', description: 'Name of the Docker container')
        string(name: 'IMAGE_NAME', defaultValue: 'queueless:latest', description: 'Docker image tag')
    }

    environment {
        APP_NAME = 'queueless'
        APP_VERSION = '1.0.0'
        TARGET_PORT = "${params.APP_PORT}"
        TARGET_CONTAINER = "${params.CONTAINER_NAME}"
        TARGET_IMAGE = "${params.IMAGE_NAME}"
    }

    options {
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        // ----------------------------------------------------------------------
        // STAGE 1: CHECKOUT SOURCE CODE
        // ----------------------------------------------------------------------
        stage('1. Checkout') {
            steps {
                echo 'Checking out latest project source code from Git repository...'
                checkout scm
                script {
                    if (isUnix()) {
                        sh 'git log -n 1 --oneline'
                    } else {
                        bat 'git log -n 1 --oneline'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 2: COMPILE APPLICATION
        // ----------------------------------------------------------------------
        stage('2. Compile') {
            steps {
                echo 'Compiling Java source code using Maven Wrapper...'
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw && ./mvnw compile -DskipTests=true'
                    } else {
                        bat 'call mvnw.cmd compile -DskipTests=true'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 3: RUN AUTOMATED TESTS (QUALITY GATE)
        // ----------------------------------------------------------------------
        stage('3. Test') {
            steps {
                echo 'Executing JUnit 5 test suite (17 Unit & Integration tests)...'
                script {
                    if (isUnix()) {
                        sh './mvnw test'
                    } else {
                        bat 'call mvnw.cmd test'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 4: PACKAGE ARTIFACT
        // ----------------------------------------------------------------------
        stage('4. Package') {
            steps {
                echo 'Packaging executable Spring Boot fat JAR into target/...'
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests=true'
                    } else {
                        bat 'call mvnw.cmd package -DskipTests=true'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 5: BUILD DOCKER IMAGE
        // ----------------------------------------------------------------------
        stage('5. Docker Build') {
            steps {
                echo "Building Docker container image: ${TARGET_IMAGE}..."
                script {
                    if (isUnix()) {
                        sh "docker build -t ${TARGET_IMAGE} ."
                    } else {
                        bat "docker build -t ${TARGET_IMAGE} ."
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 6: DEPLOY TO LOCAL DOCKER RUNTIME
        // ----------------------------------------------------------------------
        stage('6. Deploy') {
            steps {
                echo "Deploying container ${TARGET_CONTAINER} on host port ${TARGET_PORT}..."
                script {
                    if (isUnix()) {
                        sh """
                            docker stop ${TARGET_CONTAINER} || true
                            docker rm ${TARGET_CONTAINER} || true
                            docker run -d -p ${TARGET_PORT}:8085 --name ${TARGET_CONTAINER} --restart unless-stopped ${TARGET_IMAGE}
                        """
                    } else {
                        bat """
                            docker stop ${TARGET_CONTAINER} >nul 2>&1 || verify >nul
                            docker rm ${TARGET_CONTAINER} >nul 2>&1 || verify >nul
                            docker run -d -p ${TARGET_PORT}:8085 --name ${TARGET_CONTAINER} --restart unless-stopped ${TARGET_IMAGE}
                        """
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 7: SMOKE TEST / HEALTHCHECK VERIFICATION
        // ----------------------------------------------------------------------
        stage('7. Verify Deployment') {
            steps {
                echo "Verifying application health and responsiveness at http://localhost:${TARGET_PORT}/api/tickets/stats..."
                sleep time: 10, unit: 'SECONDS'
                script {
                    if (isUnix()) {
                        sh """
                            for i in 1 2 3 4 5; do
                                if curl -f -s http://localhost:${TARGET_PORT}/api/tickets/stats; then
                                    echo "Application is healthy and responding!"
                                    exit 0
                                fi
                                echo "Waiting for container warmup (attempt \$i/5)..."
                                sleep 3
                            done
                            echo "Application failed healthcheck verification!"
                            exit 1
                        """
                    } else {
                        bat """
                            powershell -NoProfile -ExecutionPolicy Bypass -Command "\$healthy = \$false; for (\$i = 1; \$i -le 5; \$i++) { try { \$r = Invoke-RestMethod -Uri 'http://localhost:${TARGET_PORT}/api/tickets/stats' -Method Get -TimeoutSec 5; if (\$r.success -eq \$true) { Write-Host 'Verification successful: ' (\$r | ConvertTo-Json -Compress); \$healthy = \$true; break } } catch { Write-Host 'Warmup retry '\$i'/5...'; Start-Sleep -Seconds 3 } }; if (-not \$healthy) { Write-Error 'Deployment verification failed'; exit 1 }"
                        """
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    // POST BUILD NOTIFICATIONS & REPORT ARCHIVING
    // --------------------------------------------------------------------------
    post {
        always {
            echo 'Archiving JUnit test results and Maven build artifacts...'
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
        success {
            echo "========================================================================"
            echo "SUCCESS: QueueLess v${APP_VERSION} was successfully built, tested, and deployed!"
            echo "Access URL: http://localhost:${TARGET_PORT}/"
            echo "========================================================================"
        }
        failure {
            echo "========================================================================"
            echo "FAILURE: QueueLess CI/CD pipeline failed. Inspect the console logs above."
            echo "Quality Gate enforced: Broken releases are prevented from deploying."
            echo "========================================================================"
        }
    }
}
