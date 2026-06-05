pipeline {
    agent any

    environment {
        SMTP_HOST = "${env.SMTP_HOST ?: 'mailhog'}"
        SMTP_PORT = "${env.SMTP_PORT ?: '1025'}"
        // NOTIFY_EMAIL must be set as a Jenkins env variable or via docker-compose
        // It is intentionally NOT hardcoded here — see docker-compose.jenkins.yml
    }

    stages {

        // ------------------------------------------------------------------
        // Uncomment this stage once tests are implemented (coverage >= 90%)
        //
        // stage('Test') {
        //     steps {
        //         script {
        //             def services = ['sensor-service', 'alert-service']
        //             services.each { svc ->
        //                 dir("services/${svc}") {
        //                     sh 'mvn test'
        //                 }
        //             }
        //         }
        //     }
        //     post {
        //         always {
        //             junit allowEmptyResults: true,
        //                   testResults: 'services/**/target/surefire-reports/*.xml'
        //         }
        //     }
        // }
        // ------------------------------------------------------------------

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
                // Uncomment when tests are enabled:
                // archiveArtifacts(
                //     artifacts: 'services/**/target/surefire-reports/**',
                //     allowEmptyArchive: true
                // )
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
