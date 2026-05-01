// Calculate the list of changed files
def computeChangedFiles() {
    def base = env.CHANGE_TARGET ?
        "origin/${env.CHANGE_TARGET}" :
        (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: env.GIT_PREVIOUS_COMMIT)

    def cmd = base ?
        "git diff --name-only ${base}...HEAD" :
        'git show --name-only --pretty="" HEAD'

    return sh(script: cmd, returnStdout: true)
        .trim()
        .split("\n")
        .findAll { it }
}

def getModules() {
    env.AFFECTED_MODULES?.split(',')?.collect { it.trim() }?.findAll { it } ?: []
}

pipeline {
    agent any

    tools {
        maven 'maven3'
        jdk 'jdk25'
    }

    environment {
        MVN_ARGS = '-B -ntp'
        SERVICES = 'common-library backoffice-bff cart customer inventory location media order payment-paypal payment product promotion rating search storefront-bff tax webhook sampledata recommendation delivery'
        SNYK_HOME = tool name: 'snyk@latest'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (env.CHANGE_TARGET) {
                        sh "git fetch --no-tags origin ${env.CHANGE_TARGET}"
                    }
                }
            }
        }

        stage('Gitleaks Scan') {
            steps {
                script {

                    def status = sh(
                        script: '''
                            gitleaks detect \
                                --source . \
                                --config gitleaks.toml \
                                --report-format json \
                                --report-path gitleaks-report.json \
                                --redact
                            ''',
                        returnStatus: true
                    )

                    if (status != 0) {
                        echo "GITLEAKS WARNING: secrets detected (see report)"
                        currentBuild.result = 'SUCCESS'
                    } else {
                        echo "No secrets detected"
                    }
                }
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def allModules = env.SERVICES.split(' ')
                    def changedFiles = computeChangedFiles()

                    // Detect rebuild all
                    def rebuildAll = changedFiles.any { f ->
                        f == 'pom.xml' ||
                        f.startsWith('checkstyle/')
                    }

                    // Optional: rebuild if Jenkinsfile changed
                    if (env.REBUILD_ALL_ON_JENKINSFILE?.toBoolean()) {
                        rebuildAll = rebuildAll || changedFiles.any {
                            it.equalsIgnoreCase('Jenkinsfile')
                        }
                    }

                    def affected = allModules.findAll { module ->
                        changedFiles.any { f ->
                            f == module || f.startsWith("${module}/")
                        }
                    }

                    if (rebuildAll) {
                        affected = allModules
                    }

                    affected = ['storefront-bff']

                    // Handle dependency rebuild
                    env.MVN_MAKE_FLAGS = '-am'
                    if (affected.contains('common-library')) {
                        env.MVN_MAKE_FLAGS = '-am -amd'
                    }

                    def affectedModulesCsv = affected.join(',')
                    env.AFFECTED_MODULES = affectedModulesCsv

                    // Logging
                    echo "rebuildAll=${rebuildAll}"
                    echo "Affected modules: ${affectedModulesCsv}"
                    echo "Changed files:\n${changedFiles.join('\n')}"

                    if (affectedModulesCsv?.trim()) {
                        currentBuild.description = "${env.BRANCH_NAME ?: ''} | services: ${affectedModulesCsv}"
                    } else {
                        currentBuild.description = "${env.BRANCH_NAME ?: ''} | no service changes"
                    }
                }
            }
        }



        stage('Build') {
            when {
                expression { env.AFFECTED_MODULES?.trim() }
            }
            steps {

                echo "Building affected modules: ${env.AFFECTED_MODULES}..."
                sh "mvn ${env.MVN_ARGS} -pl ${env.AFFECTED_MODULES} ${env.MVN_MAKE_FLAGS} -DskipTests clean package"
            }
        }

        stage('Unit & Integration Tests') {
            when {
                expression { env.AFFECTED_MODULES?.trim() }
            }
            steps {
                // Run the Maven verify command for the affected modules to execute tests and generate coverage reports
                sh """
                    mvn ${env.MVN_ARGS} \
                        -pl ${env.AFFECTED_MODULES} ${env.MVN_MAKE_FLAGS} \
                        verify \
                        -ff \
                        -DtrimStackTrace=true \
                        -Dsurefire.printSummary=true \
                        -Dfailsafe.printSummary=true
                """
                // Publish unit test and integration test results to Jenkins for reporting and analysis
                junit allowEmptyResults: true,
                      testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml'
            }
        }

        stage('Snyk Scan') {
            when {
                expression { env.AFFECTED_MODULES?.trim() }
            }
            steps {
                script {
                    def modules = getModules()

                    for (module in modules) {
                        module = module.trim()
                        if (!module) continue

                        echo "Running Snyk scan for service: ${module}"

                        dir(module) {

                            snykSecurity(
                                snykInstallation: 'snyk@latest',
                                snykTokenId: 'snyk-plugin-token',
                                failOnIssues: false,
                                projectName: module,
                                targetFile: 'pom.xml',
                                additionalArguments: '--json-file-output=snyk-dep.json'
                            )

                            def codeStatus = sh(
                                script: '''
                                    ${SNYK_HOME}/snyk-linux code test
                                ''',
                                returnStatus: true
                            )

                            if (codeStatus != 0) {
                                echo "SNYK WARNING: vulnerabilities detected in ${module}"
                                currentBuild.result = 'SUCCESS'
                            } else {
                                echo "No vulnerabilities detected in ${module}"
                            }
                        }
                    }
                }
            }
        }

        stage('Coverage Gate') {
            when {
                expression { env.AFFECTED_MODULES?.trim() }
            }
            steps {
                script {
                    def modules = getModules()

                    echo "Running coverage for modules: ${modules.join(', ')}"

                    // 2. Build Jacoco report paths dynamically
                    def coverageTools = modules.collect { module ->
                        [
                            parser: 'JACOCO',
                            pattern: "${module}/target/site/jacoco/jacoco.xml"
                        ]
                    }

                    // 3. Execute coverage gate
                    recordCoverage(
                        tools: coverageTools,
                        sourceCodeRetention: 'NEVER',
                        qualityGates: [
                            [
                                threshold: 70.0,
                                metric: 'LINE',
                                baseline: 'PROJECT',
                                criticality: 'FAILURE'
                            ],
                            [
                                threshold: 70.0,
                                metric: 'BRANCH',
                                baseline: 'PROJECT',
                                criticality: 'FAILURE'
                            ],
                            [
                                threshold: 70.0,
                                metric: 'INSTRUCTION',
                                baseline: 'PROJECT',
                                criticality: 'FAILURE'
                            ]
                        ]
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { env.AFFECTED_MODULES?.trim() }
            }
            steps {
                withSonarQubeEnv('Sonar-instances') {
                    sh """
                        mvn ${MVN_ARGS} \
                            -pl ${AFFECTED_MODULES} \
                            ${MVN_MAKE_FLAGS} \
                            sonar:sonar \
                            -Dsonar.projectKey=yas-project
                    """
                }
            }
        }
    }

    post {
        always {
            // Upload artifact
            archiveArtifacts allowEmptyArchive: true,
                artifacts: '**/target/*.jar'

            // Upload Gitleaks report
            archiveArtifacts allowEmptyArchive: true,
                artifacts: 'gitleaks-report.json'
        }

        success {
            echo 'Pipeline SUCCESS'
        }

        failure {
            echo 'Pipeline FAILED'
        }
    }
}
