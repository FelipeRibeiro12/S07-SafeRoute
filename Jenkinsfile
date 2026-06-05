pipeline {
    agent any

    environment {
        SMTP_HOST = "${env.SMTP_HOST ?: 'mailhog'}"
        SMTP_PORT = "${env.SMTP_PORT ?: '1025'}"
        // NOTIFY_EMAIL must be set via docker-compose — never hardcoded here
    }

    stages {

        stage('Test') {
            steps {
                script {
                    def services = ['sensor-service', 'alert-service']
                    services.each { svc ->
                        dir("services/${svc}") {
                            sh 'mvn test'
                        }
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'services/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def services = [
                        'config-server',
                        'eureka-server',
                        'api-gateway',
                        'sensor-service',
                        'alert-service'
                    ]
                    services.each { svc ->
                        dir("services/${svc}") {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts(
                    artifacts: 'services/*/target/*.jar',
                    fingerprint: true,
                    allowEmptyArchive: false
                )
                archiveArtifacts(
                    artifacts: 'services/**/target/surefire-reports/**',
                    allowEmptyArchive: true
                )
            }
        }
    }

    post {
        always {
            script {
                env.BUILD_STATUS = currentBuild.currentResult ?: 'UNKNOWN'
            }
            sh 'python3 jenkins/scripts/notify_email.py'
        }
    }
}
