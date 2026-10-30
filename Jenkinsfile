pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        pollSCM('* * * * *')     // polls GitHub every minute so builds trigger on localhost without webhook tunnel
    }

    environment {
        DOCKERHUB_USER = 'venkat96r'
        IMAGE_NAME     = "${DOCKERHUB_USER}/calculator-api"
        IMAGE_TAG      = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B clean package -DskipTests'
                    } else {
                        bat 'mvn -B clean package -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B test'
                    } else {
                        bat 'mvn -B test'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    } else {
                        bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    }
                }
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    script {
                        if (isUnix()) {
                            sh '''
                              echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                              docker push ${IMAGE_NAME}:${IMAGE_TAG}
                              docker push ${IMAGE_NAME}:latest
                              docker logout
                            '''
                        } else {
                            bat """
                              echo %DH_PASS% | docker login -u %DH_USER% --password-stdin
                              docker push ${IMAGE_NAME}:${IMAGE_TAG}
                              docker push ${IMAGE_NAME}:latest
                              docker logout
                            """
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                          export DOCKERHUB_USER IMAGE_TAG
                          docker compose pull
                          docker compose up -d --remove-orphans

                          echo "Waiting for health..."
                          for i in $(seq 1 20); do
                            if curl -fs http://localhost:8081/actuator/health | grep -q '"status":"UP"'; then
                              echo "Service is UP"; exit 0
                            fi
                            sleep 5
                          done
                          echo "Health check failed"; docker compose logs --tail=50; exit 1
                        '''
                    } else {
                        bat """
                          set DOCKERHUB_USER=${DOCKERHUB_USER}
                          set IMAGE_TAG=${IMAGE_TAG}
                          docker compose pull
                          docker compose up -d --remove-orphans
                          powershell -Command "for (\$i=1; \$i -le 20; \$i++) { if ((curl.exe -fs http://localhost:8081/actuator/health) -match '\"status\":\"UP\"') { Write-Host 'Service is UP'; exit 0 }; Start-Sleep 5 }; Write-Host 'Health check failed'; docker compose logs --tail=50; exit 1"
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                mail to: 'ramivenkat16@gmail.com',
                     subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Build succeeded.\nImage: ${IMAGE_NAME}:${IMAGE_TAG}\n${env.BUILD_URL}"
            }
        }
        failure {
            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                mail to: 'ramivenkat16@gmail.com',
                     subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                     body: "Build failed.\nSee ${env.BUILD_URL}console"
            }
        }
    }
}
