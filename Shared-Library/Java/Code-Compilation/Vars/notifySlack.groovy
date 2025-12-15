def call(String status, String slackCred) {

  def emoji  = status == 'SUCCESS' ? '✅' : '❌'
  def header = status == 'SUCCESS' ? 'BUILD SUCCESS' : 'BUILD FAILED'

  withCredentials([string(credentialsId: slackCred, variable: 'SLACK_WEBHOOK_URL')]) {
    sh """
      payload='{
        "blocks": [
          { "type": "header", "text": { "type": "plain_text", "text": "${emoji} ${header}" } },
          {
            "type": "section",
            "fields": [
              { "type": "mrkdwn", "text": "*Job:*\\n${env.JOB_NAME}" },
              { "type": "mrkdwn", "text": "*Build #:*\\n#${env.BUILD_NUMBER}" },
              { "type": "mrkdwn", "text": "*Status:*\\n${status}" }
            ]
          },
          {
            "type": "section",
            "text": { "type": "mrkdwn", "text": "*Build URL:*\\n<${env.BUILD_URL}|Open in Jenkins>" }
          }
        ]
      }'
      curl -s -X POST -H "Content-Type: application/json" \
        --data "$payload" "$SLACK_WEBHOOK_URL"
    """
  }
}
