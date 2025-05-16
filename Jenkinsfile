pipeline {
    agent any

    environment {
        // Define environment variables
        // Jenkins credentials configuration
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials') // DockerHub credentials ID stored in Jenkins
        DOCKER_IMAGE = 'iseeyourmonsters/teedy' // Your Docker Hub user name and Repository's name
        DOCKER_TAG = "${env.BUILD_NUMBER}" // Use build number as tag
    }

    stages {
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Iseeyourmonsters/Teedy.git']]
                    // Your GitHub Repository
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        // Building Docker images
        stage('Building Image') {
            steps {
                script {
                    // Assume Dockerfile is located at root
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        // Uploading Docker images into Docker Hub
        stage('Upload Image') {
            steps {
                script {
                    // Sign in to Docker Hub
                    docker.withRegistry('https://registry.hub.docker.com', 'DOCKER_HUB_CREDENTIALS') {
                        // Push image
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()

                        // Optional: tag as 'latest'
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        // Running Docker container
        stage('Run Containers') {
            steps {
                script {
                    // Stop then remove containers if they exist
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'

                    // Run Container
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run('--name teedy-container-8081 -d -p 8081:8080')

                    // Optional: list all teedy-containers
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
