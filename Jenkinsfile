pipeline {
    agent any

    environment {
        SMTP_HOST = "${env.SMTP_HOST ?: 'mailhog'}"
        SMTP_PORT = "${env.SMTP_PORT ?: '1025'}"
        ARTIFACT_DIR = 'ci-artifacts'
        // NOTIFY_EMAIL must be set via docker-compose — never hardcoded here
    }

    stages {

        stage('Test') {
            steps {
                script {
                    def services = ['sensor-service', 'alert-service']
                    services.each { svc ->
                        dir("services/${svc}") {
                            sh 'mvn verify'
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
                sh '''
                    rm -rf "$ARTIFACT_DIR"
                    mkdir -p "$ARTIFACT_DIR"

                    find services \( \
                        -path "*/target/*.jar" -o \
                        -path "*/target/surefire-reports/*" -o \
                        -path "*/target/site/jacoco/*" \
                    \) -type f | while IFS= read -r file; do
                        mkdir -p "$ARTIFACT_DIR/$(dirname "$file")"
                        cp "$file" "$ARTIFACT_DIR/$file"
                    done

                    if ! find "$ARTIFACT_DIR" -path "*/target/*.jar" -type f | grep -q .; then
                        echo "No JAR artifacts found."
                        exit 1
                    fi
                '''
                archiveArtifacts(
                    artifacts: 'ci-artifacts/**',
                    fingerprint: true,
                    allowEmptyArchive: false
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
