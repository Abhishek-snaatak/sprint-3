def call(Map config = [:]) {

  // -------------------------
  // DEFAULT CONFIG
  // -------------------------
  def gitUrl      = config.gitUrl      ?: error("gitUrl is required")
  def gitBranch   = config.gitBranch   ?: 'main'
  def mavenCmd    = config.mavenCmd    ?: 'mvn'
  def skipTests   = config.skipTests   ?: true
  def slackCred   = config.slackCred   ?: null

  def debugLog = "${env.WORKSPACE}/compile_debug.log"

  sh "mkdir -p ${env.WORKSPACE}"
  sh "touch ${debugLog}"

  try {

    stage('Checkout') {
      git url: gitUrl, branch: gitBranch
    }

    stage('Verify Java & Maven') {
      sh """
        set -xe
        java -version 2>&1 | tee -a ${debugLog}
        ${mavenCmd} -version 2>&1 | tee -a ${debugLog}
      """
    }

    stage('Clean & Compile') {
      sh """
        set -xe
        ${mavenCmd} clean compile ${skipTests ? '-DskipTests' : ''} \
        2>&1 | tee -a ${debugLog}
      """
    }

    stage('Verify Compilation Output') {
      sh """
        [ -d target/classes ] || exit 20
        ls -la target/classes
      """
    }

    stage('Archive Artifacts') {
      archiveArtifacts artifacts: 'target/classes/**', fingerprint: true
      archiveArtifacts artifacts: 'compile_debug.log', fingerprint: true
    }

    if (slackCred) {
      notifySlack('SUCCESS', slackCred)
    }

  } catch (err) {

    if (slackCred) {
      notifySlack('FAILED', slackCred)
    }

    throw err
  }
}
