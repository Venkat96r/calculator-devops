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

    tools {
        maven 'Maven3'          // must match name in Jenkins Global Tool Configuration
        jdk   'JDK17'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                  docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest .
                '''
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-cred',
                                                  usernameVariable: 'DH_USER',
                                                  passwordVariable: 'DH_PASS')]) {
                    sh '''
                      echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
                      docker push ${IMAGE_NAME}:${IMAGE_TAG}
                      docker push ${IMAGE_NAME}:latest
                      docker logout
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
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
            }
        }
    }

    post {
        success {
            mail to: 'ramivenkat16@gmail.com',
                 subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build succeeded.\nImage: ${IMAGE_NAME}:${IMAGE_TAG}\n${env.BUILD_URL}"
        }
        failure {
            mail to: 'ramivenkat16@gmail.com',
                 subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Build failed.\nSee ${env.BUILD_URL}console"
        }
    }
}
