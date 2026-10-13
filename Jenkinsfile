// ==============================================================================
// QueueLess — CI/CD Pipeline (Jenkinsfile)
// College DevOps Laboratory Project — 23IT723 (Experiments 4, 5, 9)
// Declarative Pipeline with Automated Testing, Docker Build,
// Deployment Healthcheck Verification & Automated Version History Recording
// ==============================================================================

pipeline {
    agent any

    parameters {
        string(name: 'APP_PORT', defaultValue: '8085', description: 'Host port to bind the QueueLess container')
        string(name: 'CONTAINER_NAME', defaultValue: 'queueless-app', description: 'Name of the Docker container')
        string(name: 'RELEASE_VERSION', defaultValue: '1.1.0', description: 'Semantic release version tag')
        choice(name: 'TARGET_ENV', choices: ['production', 'staging', 'docker-local'], description: 'Deployment target environment')
    }

    environment {
        APP_NAME = 'queueless'
        APP_VERSION = "${params.RELEASE_VERSION}"
        TARGET_PORT = "${params.APP_PORT}"
        TARGET_CONTAINER = "${params.CONTAINER_NAME}"
        TARGET_ENV_NAME = "${params.TARGET_ENV}"
        // Add Docker Desktop bin directory to PATH for Windows Jenkins service
        PATH = "C:\\Users\\Lenovo\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
    }

    options {
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }

    stages {
        // ----------------------------------------------------------------------
        // STAGE 1: CHECKOUT SOURCE CODE & RECORD GIT METADATA
        // ----------------------------------------------------------------------
        stage('1. Checkout & Extract Git Metadata') {
            steps {
                echo 'Checking out source code from Git repository...'
                checkout scm
                script {
                    if (isUnix()) {
                        env.GIT_COMMIT_HASH = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                        env.GIT_COMMIT_SHORT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                        env.GIT_BRANCH_NAME = env.GIT_BRANCH ?: sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                        env.GIT_COMMIT_MSG = sh(returnStdout: true, script: 'git log -1 --format=%s').trim()
                    } else {
                        env.GIT_COMMIT_HASH = powershell(returnStdout: true, script: '(git rev-parse HEAD).Trim()').trim()
                        env.GIT_COMMIT_SHORT = powershell(returnStdout: true, script: '(git rev-parse --short HEAD).Trim()').trim()
                        env.GIT_BRANCH_NAME = env.GIT_BRANCH ?: powershell(returnStdout: true, script: '(git rev-parse --abbrev-ref HEAD).Trim()').trim()
                        env.GIT_COMMIT_MSG = powershell(returnStdout: true, script: '(git log -1 --format="%s").Trim()').trim()
                    }
                    env.VERSIONED_IMAGE_TAG = "${APP_NAME}:${APP_VERSION}-${env.GIT_COMMIT_SHORT}"
                    env.LATEST_IMAGE_TAG = "${APP_NAME}:latest"

                    echo "=========================================================="
                    echo "GIT METADATA CAPTURED FOR PROVENANCE AUDIT:"
                    echo "Commit Full  : ${env.GIT_COMMIT_HASH}"
                    echo "Commit Short : ${env.GIT_COMMIT_SHORT}"
                    echo "Branch       : ${env.GIT_BRANCH_NAME}"
                    echo "Message      : ${env.GIT_COMMIT_MSG}"
                    echo "Build Number : ${env.BUILD_NUMBER}"
                    echo "Target Image : ${env.VERSIONED_IMAGE_TAG}"
                    echo "=========================================================="
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
                echo 'Executing JUnit 5 test suite (Queue & Version History tests)...'
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
        // STAGE 5: BUILD VERSIONED DOCKER IMAGE
        // ----------------------------------------------------------------------
        stage('5. Docker Build') {
            steps {
                echo "Building versioned Docker image: ${VERSIONED_IMAGE_TAG} and ${LATEST_IMAGE_TAG}..."
                script {
                    if (isUnix()) {
                        sh "docker build -t ${LATEST_IMAGE_TAG} -t ${VERSIONED_IMAGE_TAG} ."
                    } else {
                        bat """
                            @echo off
                            where docker >nul 2>&1 || set "PATH=C:\\Users\\Lenovo\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;%PATH%"
                            docker build -t ${LATEST_IMAGE_TAG} -t ${VERSIONED_IMAGE_TAG} .
                        """
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 6: DEPLOY TO DOCKER RUNTIME (WITH PERSISTENT STORAGE)
        // ----------------------------------------------------------------------
        stage('6. Deploy') {
            steps {
                echo "Deploying container ${TARGET_CONTAINER} with image ${VERSIONED_IMAGE_TAG} on host port ${TARGET_PORT}..."
                script {
                    def deployAction = {
                        if (isUnix()) {
                            sh '''
                                docker stop "${TARGET_CONTAINER}" 2>/dev/null || true
                                docker rm "${TARGET_CONTAINER}" 2>/dev/null || true
                                docker run -d -p "${TARGET_PORT}:8085" \
                                    -v queueless-data:/app/data \
                                    -e SERVER_PORT=8085 \
                                    -e RELEASE_TRACKER_TOKEN \
                                    --name "${TARGET_CONTAINER}" \
                                    --restart unless-stopped \
                                    "${VERSIONED_IMAGE_TAG}"
                            '''
                        } else {
                            powershell '''
                                where.exe docker >$null 2>&1
                                docker stop $env:TARGET_CONTAINER 2>$null
                                docker rm $env:TARGET_CONTAINER 2>$null
                                docker run -d -p "${env:TARGET_PORT}:8085" `
                                    -v queueless-data:/app/data `
                                    -e SERVER_PORT=8085 `
                                    -e RELEASE_TRACKER_TOKEN `
                                    --name $env:TARGET_CONTAINER `
                                    --restart unless-stopped `
                                    $env:VERSIONED_IMAGE_TAG
                            '''
                        }
                    }

                    try {
                        withCredentials([string(credentialsId: 'queueless-release-token', variable: 'RELEASE_TRACKER_TOKEN')]) {
                            deployAction()
                        }
                    } catch (Exception e) {
                        try {
                            withCredentials([string(credentialsId: 'RELEASE_TRACKER_TOKEN', variable: 'RELEASE_TRACKER_TOKEN')]) {
                                deployAction()
                            }
                        } catch (Exception e2) {
                            deployAction()
                        }
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 7: HEALTHCHECK VERIFICATION
        // ----------------------------------------------------------------------
        stage('7. Verify Deployment') {
            steps {
                echo "Verifying application health and responsiveness at http://localhost:${TARGET_PORT}/api/tickets/stats..."
                sleep time: 8, unit: 'SECONDS'
                script {
                    if (isUnix()) {
                        sh """
                            for i in 1 2 3 4 5 6; do
                                if curl -f -s http://localhost:${TARGET_PORT}/api/tickets/stats; then
                                    echo "Application is healthy and responding!"
                                    exit 0
                                fi
                                echo "Waiting for container warmup (attempt \$i/6)..."
                                sleep 3
                            done
                            echo "Application failed healthcheck verification!"
                            exit 1
                        """
                    } else {
                        bat """
                            powershell -NoProfile -ExecutionPolicy Bypass -Command "\$healthy = \$false; for (\$i = 1; \$i -le 6; \$i++) { try { \$r = Invoke-RestMethod -Uri 'http://localhost:${TARGET_PORT}/api/tickets/stats' -Method Get -TimeoutSec 5; if (\$r.success -eq \$true) { Write-Host 'Verification successful: ' (\$r | ConvertTo-Json -Compress); \$healthy = \$true; break } } catch { Write-Host 'Warmup retry '\$i'/6...'; Start-Sleep -Seconds 3 } }; if (-not \$healthy) { Write-Error 'Deployment verification failed'; exit 1 }"
                        """
                    }
                }
            }
        }

        // ----------------------------------------------------------------------
        // STAGE 8: RECORD RELEASE METADATA (AUDIT & VERSION MONITORING)
        // ----------------------------------------------------------------------
        stage('8. Record Release Metadata') {
            steps {
                echo "Recording verified deployment metadata in QueueLess Version History..."
                script {
                    def commitMsg = env.GIT_COMMIT_MSG ?: ""
                    if (commitMsg.contains("\n")) {
                        commitMsg = commitMsg.split("\n")[0].trim()
                    }
                    if (commitMsg.contains("\r")) {
                        commitMsg = commitMsg.split("\r")[0].trim()
                    }

                    def testSummary = ""
                    if (isUnix()) {
                        testSummary = sh(returnStdout: true, script: '''
                            reports_dir="target/surefire-reports"
                            if [ ! -d "$reports_dir" ]; then
                                echo "ERROR: Surefire reports directory not found: $reports_dir" >&2
                                exit 1
                            fi
                            files=$(ls "$reports_dir"/TEST-*.xml 2>/dev/null || ls "$reports_dir"/*.xml 2>/dev/null)
                            if [ -z "$files" ]; then
                                echo "ERROR: No Surefire test report XML files found in $reports_dir" >&2
                                exit 1
                            fi
                            awk '
                            BEGIN {
                                total = 0; failures = 0; errors = 0; skipped = 0; count = 0;
                            }
                            /<testsuite/ {
                                count++;
                                for (i = 1; i <= NF; i++) {
                                    if ($i ~ /^tests=/) { match($i, /[0-9]+/); total += substr($i, RSTART, RLENGTH); }
                                    if ($i ~ /^failures=/) { match($i, /[0-9]+/); failures += substr($i, RSTART, RLENGTH); }
                                    if ($i ~ /^errors=/) { match($i, /[0-9]+/); errors += substr($i, RSTART, RLENGTH); }
                                    if ($i ~ /^skipped=/) { match($i, /[0-9]+/); skipped += substr($i, RSTART, RLENGTH); }
                                }
                            }
                            END {
                                if (count == 0 || total == 0) {
                                    print "ERROR: Zero tests found in Surefire reports" > "/dev/stderr";
                                    exit 1;
                                }
                                if (failures > 0 || errors > 0) {
                                    printf("ERROR: Test suite failed with %d failures and %d errors out of %d tests\\n", failures, errors, total) > "/dev/stderr";
                                    exit 1;
                                }
                                passed = total - failures - errors - skipped;
                                if (skipped > 0) {
                                    printf("PASSED (%d/%d tests, %d skipped)\\n", passed, total, skipped);
                                } else {
                                    printf("PASSED (%d/%d tests)\\n", total, total);
                                }
                            }' $files
                        ''').trim()
                    } else {
                        testSummary = powershell(returnStdout: true, script: '''
                            $reportsDir = "target/surefire-reports"
                            if (-not (Test-Path $reportsDir)) {
                                Write-Error "Surefire reports directory not found: $reportsDir"
                                exit 1
                            }

                            $files = Get-ChildItem -Path $reportsDir -Filter "TEST-*.xml" -ErrorAction SilentlyContinue
                            if (-not $files -or $files.Count -eq 0) {
                                $files = Get-ChildItem -Path $reportsDir -Filter "*.xml" -ErrorAction SilentlyContinue
                            }
                            if (-not $files -or $files.Count -eq 0) {
                                Write-Error "No Surefire test report XML files found in $reportsDir"
                                exit 1
                            }

                            $total = 0
                            $failures = 0
                            $errors = 0
                            $skipped = 0

                            foreach ($f in $files) {
                                try {
                                    [xml]$xml = Get-Content -LiteralPath $f.FullName -ErrorAction Stop
                                } catch {
                                    Write-Error "Failed to parse test report XML: $($f.FullName) - $($_.Exception.Message)"
                                    exit 1
                                }

                                $suites = $xml.SelectNodes("//testsuite")
                                if (-not $suites -or $suites.Count -eq 0) {
                                    if ($xml.testsuite) {
                                        $suites = @($xml.testsuite)
                                    }
                                }

                                foreach ($suite in $suites) {
                                    $testsAttr = $suite.GetAttribute("tests")
                                    $failuresAttr = $suite.GetAttribute("failures")
                                    $errorsAttr = $suite.GetAttribute("errors")
                                    $skippedAttr = $suite.GetAttribute("skipped")

                                    if ($testsAttr) { $total += [int]$testsAttr }
                                    if ($failuresAttr) { $failures += [int]$failuresAttr }
                                    if ($errorsAttr) { $errors += [int]$errorsAttr }
                                    if ($skippedAttr) { $skipped += [int]$skippedAttr }
                                }
                            }

                            if ($total -le 0) {
                                Write-Error "Zero tests found across all Surefire reports in $reportsDir"
                                exit 1
                            }

                            if ($failures -gt 0 -or $errors -gt 0) {
                                Write-Error "Test suite failed with $failures failures and $errors errors out of $total tests"
                                exit 1
                            }

                            $passed = $total - $failures - $errors - $skipped
                            if ($skipped -gt 0) {
                                "PASSED ({0}/{1} tests, {2} skipped)" -f $passed, $total, $skipped
                            } else {
                                "PASSED ({0}/{1} tests)" -f $total, $total
                            }
                        ''').trim()
                    }

                    if (!testSummary || !testSummary.startsWith("PASSED")) {
                        error("Release metadata verification failed: invalid test result summary '${testSummary}'")
                    }
                    echo "Verified automated test results from Surefire reports: ${testSummary}"

                    def metadata = [
                        version: APP_VERSION,
                        gitCommit: env.GIT_COMMIT_HASH,
                        gitCommitShort: env.GIT_COMMIT_SHORT,
                        gitBranch: env.GIT_BRANCH_NAME,
                        commitMessage: commitMsg,
                        jenkinsBuildNumber: env.BUILD_NUMBER,
                        jenkinsBuildUrl: env.BUILD_URL ?: ("http://localhost:8080/job/" + env.JOB_NAME + "/" + env.BUILD_NUMBER + "/"),
                        testResult: testSummary,
                        deploymentStatus: "VERIFIED",
                        environment: TARGET_ENV_NAME,
                        dockerImageTag: VERSIONED_IMAGE_TAG,
                        pipelineJob: env.JOB_NAME,
                        deploymentId: "${env.JOB_NAME}-${env.BUILD_NUMBER}"
                    ]
                    writeFile file: 'target/release-metadata.json', text: groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(metadata))

                    def recordAction = {
                        if (isUnix()) {
                            sh '''
                                headers=()
                                headers+=(-H "Content-Type: application/json")
                                if [ -n "$RELEASE_TRACKER_TOKEN" ]; then
                                    headers+=(-H "X-Release-Token: $RELEASE_TRACKER_TOKEN")
                                fi
                                http_code=$(curl -s -o target/release-response.json -w "%{http_code}" -X POST "http://localhost:${TARGET_PORT}/api/version-history" "${headers[@]}" -d @target/release-metadata.json)
                                if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
                                    echo "Release successfully recorded (HTTP $http_code)"
                                else
                                    echo "ERROR: Version history recording failed (HTTP $http_code): $(cat target/release-response.json 2>/dev/null)"
                                    exit 1
                                fi
                            '''
                        } else {
                            powershell '''
                                $headers = @{ 'Content-Type' = 'application/json' }
                                if ($env:RELEASE_TRACKER_TOKEN) {
                                    $headers['X-Release-Token'] = $env:RELEASE_TRACKER_TOKEN
                                }
                                try {
                                    $res = Invoke-RestMethod -Uri "http://localhost:${env:TARGET_PORT}/api/version-history" -Method Post -Headers $headers -InFile 'target/release-metadata.json'
                                    Write-Host "Release successfully recorded: $($res.message)"
                                } catch {
                                    $statusCode = "Unknown"
                                    $errorBody = ""
                                    if ($_.Exception.Response) {
                                        $statusCode = [int]$_.Exception.Response.StatusCode
                                        try {
                                            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                                            $errorBody = $reader.ReadToEnd()
                                        } catch {}
                                    }
                                    Write-Error "Version history recording failed (HTTP $statusCode): $errorBody - $($_.Exception.Message)"
                                    exit 1
                                }
                            '''
                        }
                    }

                    try {
                        withCredentials([string(credentialsId: 'queueless-release-token', variable: 'RELEASE_TRACKER_TOKEN')]) {
                            recordAction()
                        }
                    } catch (Exception e) {
                        try {
                            withCredentials([string(credentialsId: 'RELEASE_TRACKER_TOKEN', variable: 'RELEASE_TRACKER_TOKEN')]) {
                                recordAction()
                            }
                        } catch (Exception e2) {
                            recordAction()
                        }
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
            archiveArtifacts artifacts: 'target/*.jar, target/release-metadata.json', allowEmptyArchive: true
        }
        success {
            echo "========================================================================"
            echo "SUCCESS: QueueLess v${APP_VERSION} [${env.GIT_COMMIT_SHORT}] deployed and verified!"
            echo "Version History Dashboard : http://localhost:${TARGET_PORT}/#version-view"
            echo "Jenkins Pipeline Run      : ${env.BUILD_URL}"
            echo "========================================================================"
        }
        failure {
            echo "========================================================================"
            echo "FAILURE: QueueLess CI/CD pipeline failed. Inspect the console logs above."
            echo "Quality Gate enforced: Broken builds are NEVER recorded as active releases."
            echo "Existing deployed release remains active and protected."
            echo "========================================================================"
        }
    }
}
