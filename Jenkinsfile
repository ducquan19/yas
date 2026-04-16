// Helper: run shell command and capture output (Linux only)
def runCapture(String cmd) {
  return sh(script: cmd, returnStdout: true).trim()
}

// Compute changed files between commits or PR
def computeChangedFiles() {
  def cmd = null

  if (env.CHANGE_TARGET) {
    // PR build: compare with target branch
    cmd = "git diff --name-only origin/${env.CHANGE_TARGET}...HEAD"
  } else if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT && env.GIT_COMMIT) {
    cmd = "git diff --name-only ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${env.GIT_COMMIT}"
  } else if (env.GIT_PREVIOUS_COMMIT && env.GIT_COMMIT) {
    cmd = "git diff --name-only ${env.GIT_PREVIOUS_COMMIT}..${env.GIT_COMMIT}"
  } else {
    cmd = 'git show --name-only --pretty="" HEAD'
  }

  try {
    def out = runCapture(cmd)
    return out.split(/\r?\n/).collect { it.trim() }.findAll { it }
  } catch (err) {
    def out = runCapture('git show --name-only --pretty="" HEAD')
    return out.split(/\r?\n/).collect { it.trim() }.findAll { it }
  }
}

// Read all Maven modules from root pom.xml
def readMavenModulesFromRootPom() {
  def pom = readFile('pom.xml')
  def matcher = (pom =~ /<module>([^<]+)<\/module>/)
  def modules = []
  matcher.each { m -> modules << m[1].trim() }
  return modules.unique()
}

pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    skipDefaultCheckout(true)
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  triggers {
    pollSCM('H/15 * * * *')
  }

  environment {
    MVN_ARGS = '-B -ntp'
    AFFECTED_MODULES = ''
    MVN_MAKE_FLAGS = '-am'
  }

  stages {

    stage('Checkout code') {
      steps {
        checkout scm
        // Fetch full refs to ensure diff works correctly
        sh 'git fetch --no-tags --prune origin +refs/heads/*:refs/remotes/origin/*'
      }
    }

    stage('Detect changed services (monorepo)') {
      steps {
        script {
          def allModules = readMavenModulesFromRootPom()
          def changedFiles = computeChangedFiles()

          // If core files change → rebuild everything
          def rebuildAll = changedFiles.any { f ->
            f == 'pom.xml' ||
            f == 'Jenkinsfile' ||
            f.startsWith('checkstyle/')
          }

          // Extract top-level directories from changed files
          def touchedTopDirs = changedFiles
            .findAll { it.contains('/') }
            .collect { it.tokenize('/')[0] }
            .unique()

          // Match directories with Maven modules
          def affected = touchedTopDirs.findAll { d -> allModules.contains(d) }

          if (rebuildAll) {
            affected = allModules
          }

          // If shared library changes → rebuild dependents
          if (affected.contains('common-library')) {
            env.MVN_MAKE_FLAGS = '-am -amd'
          }

          env.AFFECTED_MODULES = affected.join(',')

          if (env.AFFECTED_MODULES?.trim()) {
            currentBuild.description = "${env.BRANCH_NAME ?: ''} | modules: ${env.AFFECTED_MODULES}"
            echo "Changed files:\n${changedFiles.join('\n')}"
            echo "Affected Maven modules: ${env.AFFECTED_MODULES}"
          } else {
            currentBuild.description = "${env.BRANCH_NAME ?: ''} | no service changes"
            echo "Changed files:\n${changedFiles.join('\n')}"
            echo 'No Maven service module changed; skipping Test/Build.'
          }
        }
      }
    }

    stage('Install dependencies') {
      steps {
        // Pre-download dependencies to speed up build
        sh "mvn ${env.MVN_ARGS} -DskipTests dependency:go-offline"
      }
    }

    stage('Test (upload results + coverage)') {
      when {
        expression { return env.AFFECTED_MODULES?.trim() }
      }
      steps {
        script {
          def mods = env.AFFECTED_MODULES
          sh "mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} verify"
        }
      }
    }

    stage('Build') {
      when {
        expression { return env.AFFECTED_MODULES?.trim() }
      }
      steps {
        script {
          def mods = env.AFFECTED_MODULES
          sh "mvn ${env.MVN_ARGS} -pl ${mods} ${env.MVN_MAKE_FLAGS} -DskipTests package"
        }
      }
    }
  }

  post {
    always {
      // Publish JUnit test results
      junit allowEmptyResults: true,
            testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'

      // Archive coverage artifacts (JaCoCo)
      archiveArtifacts allowEmptyArchive: true,
                       artifacts: '**/target/site/jacoco/**,**/target/jacoco.exec'

      // Archive build artifacts
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
      echo 'Pipeline unstable (test failures or quality gates).'
    }
  }
}