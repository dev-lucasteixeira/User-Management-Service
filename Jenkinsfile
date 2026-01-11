pipeline {
	agent any

	environment {
		DOCKER_IMAGE = "lucasteixeiralr/user-management-service"

		DOCKER_TAG = "latest"

		DOCKER_CREDENTIALS_ID = "docker-hub-credentials"
	}

	stages {
		stage('Checkout') {
			steps {
				checkout scm
			}
		}

		stage('Setup e Permissões') {
			steps {
				sh 'chmod +x gradlew'
			}
		}

		stage('Build JAR') {
			steps {
				echo 'Gerando .jar'
				sh './gradlew clean build -x test'
			}
		}

		stage('Docker Build & Push') {
			steps {
				script {
					docker.withRegistry('', DOCKER_CREDENTIALS_ID) {

						echo "Build Image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
						def customImage = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")

						echo "push to dockerhub"
						customImage.push()
					}
				}
			}
		}
	}

	post {
		always {
			cleanWs()
		}
		success {
			echo 'Sucesso!'
		}
		failure {
			echo 'Falha!'
		}
	}
}