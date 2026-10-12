// ==============================================================================
// QueueLess — CI/CD Pipeline (Jenkinsfile)
// College DevOps Laboratory Project — 23IT723 (Experiments 4, 5, 9)
// Declarative Pipeline with Automated Testing, Docker Build & Container Deployment
// ==============================================================================

pipeline {
    agent any

    tools {
        // Maven and JDK tool names configured in Jenkins Global Tool Configuration
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }

    environment {
        APP_NAME = 'queueless'
        APP_VERSION = '1.0.0'
        IMAGE_NAME = 'queueless:latest'
        CONTAINER_NAME = 'queueless-app'
        APP_PORT = '8085'
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
        stage('1. Checkout Source Code') {
            steps {
                echo 'Checking out latest commit from Git repository...'
                checkout scm
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 2: COMPILE APPLICATION
        // ----------------------------------------------------------------------
        stage('2. Compile Application') {
            steps {
                echo 'Compiling Java source code with Maven...'
                script {
                    if (isUnix()) {
                        sh './mvnw compile'
                    } else {
                        bat 'mvnw.cmd compile'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 3: AUTOMATED TESTING (JUNIT 5)
        // ----------------------------------------------------------------------
        stage('3. Run Automated Tests') {
            steps {
                echo 'Running JUnit 5 unit and integration test suite...'
                script {
                    if (isUnix()) {
                        sh './mvnw test'
                    } else {
                        bat 'mvnw.cmd test'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 4: PACKAGE ARTIFACT
        // ----------------------------------------------------------------------
        stage('4. Package Fat JAR') {
            steps {
                echo 'Packaging executable Spring Boot fat JAR...'
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests=true'
                    } else {
                        bat 'mvnw.cmd package -DskipTests=true'
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 5: BUILD DOCKER IMAGE
        // ----------------------------------------------------------------------
        stage('5. Build Docker Image') {
            steps {
                echo "Building Docker container image: ${IMAGE_NAME}..."
                script {
                    if (isUnix()) {
                        sh "docker build -t ${IMAGE_NAME} ."
                    } else {
                        bat "docker build -t ${IMAGE_NAME} ."
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 6: DEPLOY TO LOCAL DOCKER RUNTIME
        // ----------------------------------------------------------------------
        stage('6. Deploy Container') {
            steps {
                echo "Deploying container ${CONTAINER_NAME} on port ${APP_PORT}..."
                script {
                    if (isUnix()) {
                        sh """
                            docker stop ${CONTAINER_NAME} || true
                            docker rm ${CONTAINER_NAME} || true
                            docker run -d -p ${APP_PORT}:8085 --name ${CONTAINER_NAME} ${IMAGE_NAME}
                        """
                    } else {
                        bat """
                            docker stop ${CONTAINER_NAME} || verify > nul
                            docker rm ${CONTAINER_NAME} || verify > nul
                            docker run -d -p ${APP_PORT}:8085 --name ${CONTAINER_NAME} ${IMAGE_NAME}
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
                echo "Verifying deployment health at http://localhost:${APP_PORT}/api/tickets/stats..."
                sleep time: 10, unit: 'SECONDS'
                script {
                    if (isUnix()) {
                        sh "curl -f -s http://localhost:${APP_PORT}/api/tickets/stats || exit 1"
                    } else {
                        bat "powershell -Command \"Invoke-RestMethod -Uri 'http://localhost:${APP_PORT}/api/tickets/stats' -Method Get\""
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
            echo "SUCCESS: QueueLess v${APP_VERSION} was successfully built, tested, and deployed!"
        }
        failure {
            echo "FAILURE: QueueLess CI/CD pipeline failed. Inspect the console logs above for diagnostics."
        }
    }
}
