pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'oosalah02/vr-account-service'
        TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Build & Test') {
            steps {
                sh 'mvn -version'
                sh 'mvn -B clean package'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${TAG} -t ${DOCKER_IMAGE}:latest ."
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${TAG}").push()
                        docker.image("${DOCKER_IMAGE}:latest").push()
                    }
                }
            }
        }
    }

    post {
        success { echo "Successfully built and pushed ${DOCKER_IMAGE}:${TAG}" }
        failure { echo 'Pipeline failed. Check the console output.' }
    }
}