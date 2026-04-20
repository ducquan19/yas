def runCmd(String cmd) {
  if (isUnix()) {
    sh cmd
  } else {
    bat cmd
  }
}

def runCapture(String cmd) {
  if (isUnix()) {
    return sh(script: cmd, returnStdout: true).trim()
  }
  return bat(script: cmd, returnStdout: true).trim()
}

def runStatus(String cmd) {
  if (isUnix()) {
    return sh(script: cmd, returnStatus: true)
  }
  return bat(script: cmd, returnStatus: true)
}

def computeChangedFiles() {
  def cmd

  if (env.CHANGE_TARGET) {
    cmd = "git -c color.ui=never diff --name-only origin/${env.CHANGE_TARGET}...HEAD"
  } else if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT && env.GIT_COMMIT) {
    cmd = "git -c color.ui=never diff --name-only ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${env.GIT_COMMIT}"
  } else if (env.GIT_PREVIOUS_COMMIT && env.GIT_COMMIT) {
    cmd = "git -c color.ui=never diff --name-only ${env.GIT_PREVIOUS_COMMIT}..${env.GIT_COMMIT}"
  } else {
    cmd = 'git -c color.ui=never show --name-only --pretty="" HEAD'
  }

  try {
    return runCapture(cmd)
      .split(/\r?\n/)
      .collect { it.trim() }
      .findAll { it }
  } catch (err) {
    return runCapture('git -c color.ui=never show --name-only --pretty="" HEAD')
      .split(/\r?\n/)
      .collect { it.trim() }
      .findAll { it }
  }
}

def readMavenModulesFromRootPom() {
  def pom = readFile('pom.xml')
  def matcher = (pom =~ /<module>([^<]+)<\/module>/)
  def modules = []
  matcher.each { m -> modules << m[1].trim() }
  return modules.unique()
}

def readAffectedModulesCsv() {
  return fileExists('.jenkins_affected_modules') ? readFile('.jenkins_affected_modules').trim() : (env.AFFECTED_MODULES ?: '').trim()
}

def readAffectedModulesList() {
  return readAffectedModulesCsv()
    .split(',')
    .collect { it.trim() }
    .findAll { it }
}

def readSonarProjectKeyFromModulePom(String module) {
  def pomPath = "${module}/pom.xml"
  if (!fileExists(pomPath)) {
    return ''
  }

  def pom = readFile(pomPath)
  def matcher = (pom =~ /<sonar\.projectKey>([^<]+)<\/sonar\.projectKey>/)
  if (matcher.find()) {
    return matcher.group(1).trim()
  }
  return ''
}

pipeline {
  agent any

  tools {
    jdk 'jdk25'
    maven 'maven3'
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    skipDefaultCheckout(true)
    overrideIndexTriggers(true)
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  // Multibranch should normally be triggered by webhook/indexing.
  triggers {
    pollSCM('H/15 * * * *')
  }

  environment {
    MVN_ARGS = '-B -ntp'
    AFFECTED_MODULES = ''
    MVN_MAKE_FLAGS = '-am'
    REBUILD_ALL_ON_JENKINSFILE = 'false'
    SKIP_PIPELINE = 'false'
    GITLEAKS_FAIL_ON_FINDINGS = 'false'
    SONAR_TOKEN = credentials('sonar-token')
    SNYK_TOKEN = credentials('snyk-token')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        script {
          runCmd('git fetch --no-tags --prune origin +refs/heads/*:refs/remotes/origin/*')
        }
      }
    }

    stage('Detect Changed Modules') {
      steps {
        script {
          def allModules = readMavenModulesFromRootPom()
          def changedFiles = computeChangedFiles()

          def normalizedChangedFiles = changedFiles
            .collect { it.replaceAll('\\u001B\\[[;\\d]*m', '').trim() }
            .collect { it.replace('\\', '/') }
            .collect { it.replaceFirst(/^\.\//, '') }
            .findAll { it }

          def rebuildAll = normalizedChangedFiles.any { f ->
            f.equalsIgnoreCase('pom.xml') || f.startsWith('checkstyle/')
          }

          if (env.REBUILD_ALL_ON_JENKINSFILE?.toBoolean()) {
            rebuildAll = rebuildAll || normalizedChangedFiles.any { f -> f.equalsIgnoreCase('Jenkinsfile') }
          }

          def affectedModules = allModules.findAll { module ->
            normalizedChangedFiles.any { f -> f == module || f.startsWith("${module}/") }
          }

          if (rebuildAll) {
            affectedModules = allModules
          }

          if (affectedModules.contains('common-library')) {
            env.MVN_MAKE_FLAGS = '-am -amd'
          }

          def affectedModulesCsv = affectedModules ? affectedModules.join(',') : ''
          env.AFFECTED_MODULES = affectedModulesCsv
          env.SKIP_PIPELINE = env.AFFECTED_MODULES?.trim() ? 'false' : 'true'
          writeFile file: '.jenkins_affected_modules', text: (affectedModulesCsv ?: '')

          if (env.SKIP_PIPELINE == 'true') {
            currentBuild.description = "${env.BRANCH_NAME ?: ''} | no Maven service changes"
            echo "Changed files:\n${normalizedChangedFiles.join('\n')}"
            echo 'No impacted Maven module. Test/Coverage/Build stages will be skipped.'
          } else {
            currentBuild.description = "${env.BRANCH_NAME ?: ''} | modules: ${affectedModulesCsv}"
            echo "Changed files:\n${normalizedChangedFiles.join('\n')}"
            echo "Affected modules: ${affectedModulesCsv}"
          }
        }
      }
    }

    stage('Test') {
      when {
        expression { env.SKIP_PIPELINE != 'true' }
      }
      steps {
        script {
          def mods = readAffectedModulesCsv()
          int verifyStatus = runStatus("mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} verify")
          if (verifyStatus != 0) {
            currentBuild.result = 'FAILURE'
            echo 'verify failed; running jacoco:report fallback to publish coverage artifacts.'
            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
              runCmd("mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} -DskipTests jacoco:report")
            }
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true,
                testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
        }
      }
    }

    stage('Coverage Gate (>70%)') {
      when {
        expression { env.SKIP_PIPELINE != 'true' }
      }
      steps {
        script {
          def mods = readAffectedModulesCsv()
          def coverageTools = mods
            .split(',')
            .collect { it.trim() }
            .findAll { it }
            .collect { module ->
              [parser: 'JACOCO', pattern: "${module}/target/site/jacoco/jacoco.xml"]
            }

          recordCoverage(
            tools: coverageTools,
            sourceCodeRetention: 'NEVER',
            qualityGates: [
              [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', criticality: 'FAILURE'],
              [threshold: 50.0, metric: 'BRANCH', baseline: 'PROJECT', criticality: 'FAILURE'],
              [threshold: 70.0, metric: 'INSTRUCTION', baseline: 'PROJECT', criticality: 'UNSTABLE']
            ]
          )
        }
      }
    }

    stage('Gitleaks Scan') {
      when {
        expression { env.SKIP_PIPELINE != 'true' }
      }
      steps {
        script {
          int gitleaksStatus
          if (isUnix()) {
            gitleaksStatus = runStatus('''
              if command -v gitleaks >/dev/null 2>&1; then
                gitleaks detect --source . --config gitleaks.toml --no-git --verbose --report-format sarif --report-path gitleaks-report.sarif
              else
                docker run --rm -v "$PWD:/work" -w /work zricethezav/gitleaks:v8.18.4 detect --source . --config /work/gitleaks.toml --no-git --verbose --report-format sarif --report-path /work/gitleaks-report.sarif
              fi
            ''')
          } else {
            gitleaksStatus = runStatus('''
              where gitleaks >nul 2>nul
              if %ERRORLEVEL% EQU 0 (
                gitleaks detect --source . --config gitleaks.toml --no-git --verbose --report-format sarif --report-path gitleaks-report.sarif
              ) else (
                docker run --rm -v "%CD%:/work" -w /work zricethezav/gitleaks:v8.18.4 detect --source . --config /work/gitleaks.toml --no-git --verbose --report-format sarif --report-path /work/gitleaks-report.sarif
              )
            ''')
          }

          if (gitleaksStatus != 0) {
            def msg = 'Gitleaks found potential secrets. Review gitleaks-report.sarif and rotate/revoke exposed credentials if needed.'
            if (env.GITLEAKS_FAIL_ON_FINDINGS?.toBoolean()) {
              error(msg)
            } else {
              unstable(msg)
            }
          }
        }
      }
    }

    stage('SonarQube Scan') {
      when {
        expression { env.SKIP_PIPELINE != 'true' }
      }
      steps {
        script {
          if (!(env.SONAR_TOKEN ?: '').trim()) {
            error('SONAR_TOKEN is required for SonarQube scan. Configure it in Jenkins credentials/environment.')
          }

          def moduleList = readAffectedModulesList()
          moduleList.each { module ->
            def sonarProjectKey = readSonarProjectKeyFromModulePom(module)
            if (!sonarProjectKey) {
              error("Missing <sonar.projectKey> in ${module}/pom.xml")
            }

            runCmd("mvn ${env.MVN_ARGS} -pl ${module} ${env.MVN_MAKE_FLAGS} org.sonarsource.scanner.maven:sonar-maven-plugin:5.6.0.6792:sonar -Dsonar.token=${env.SONAR_TOKEN} -Dsonar.projectKey=${sonarProjectKey}")
          }
        }
      }
    }

    stage('Snyk Scan') {
      when {
        expression { env.SKIP_PIPELINE != 'true' && (env.SNYK_TOKEN ?: '').trim() }
      }
      steps {
        script {
          def moduleList = readAffectedModulesList()

          if (isUnix()) {
            sh '''
              if ! command -v snyk >/dev/null 2>&1; then
                npm install -g snyk
              fi
            '''
          } else {
            bat '''
              where snyk >nul 2>nul
              if %ERRORLEVEL% NEQ 0 npm install -g snyk
            '''
          }

          if (isUnix()) {
            sh "snyk auth ${env.SNYK_TOKEN}"
          } else {
            bat "snyk auth ${env.SNYK_TOKEN}"
          }

          moduleList.each { module ->
            runCmd("snyk test --file=${module}/pom.xml --package-manager=maven")
          }
        }
      }
    }

    stage('Build') {
      when {
        expression {
          env.SKIP_PIPELINE != 'true' && (currentBuild.currentResult == null || currentBuild.currentResult == 'SUCCESS')
        }
      }
      steps {
        script {
          def mods = readAffectedModulesCsv()
          runCmd("mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} -DskipTests package")
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts allowEmptyArchive: true,
                       artifacts: '**/target/site/jacoco/**,**/target/jacoco.exec'
      archiveArtifacts allowEmptyArchive: true,
                       artifacts: 'gitleaks-report.sarif'
      archiveArtifacts allowEmptyArchive: true,
                       artifacts: '**/target/*.jar,**/target/*.war'
    }

    success {
      echo 'Pipeline succeeded.'
    }

    failure {
      echo 'Pipeline failed.'
    }

    unstable {
      echo 'Pipeline unstable.'
    }
  }
}
