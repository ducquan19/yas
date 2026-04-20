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

          env.AFFECTED_MODULES = affectedModules.join(',')
          env.SKIP_PIPELINE = env.AFFECTED_MODULES?.trim() ? 'false' : 'true'
          writeFile file: '.jenkins_affected_modules', text: env.AFFECTED_MODULES

          if (env.SKIP_PIPELINE == 'true') {
            currentBuild.description = "${env.BRANCH_NAME ?: ''} | no Maven service changes"
            echo "Changed files:\n${normalizedChangedFiles.join('\n')}"
            echo 'No impacted Maven module. Test/Coverage/Build stages will be skipped.'
          } else {
            currentBuild.description = "${env.BRANCH_NAME ?: ''} | modules: ${env.AFFECTED_MODULES}"
            echo "Changed files:\n${normalizedChangedFiles.join('\n')}"
            echo "Affected modules: ${env.AFFECTED_MODULES}"
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
          catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
            runCmd("mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} verify")
          }

          // Always try to generate jacoco.xml for coverage publishing.
          catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
            runCmd("mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} -DskipTests jacoco:report")
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
        recordCoverage(
          tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
          qualityGates: [
            [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', failure: true],
            [threshold: 70.0, metric: 'BRANCH', baseline: 'PROJECT', failure: true]
          ]
        )
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
