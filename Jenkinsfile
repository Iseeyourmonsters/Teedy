pipeline {
    agent any
    environment {
        // 🟡🟡🟡 需填写以下三个变量 🟡🟡🟡
        DEPLOYMENT_NAME = "teedy-docker-deployment"    // 你的 Kubernetes Deployment 名称
        CONTAINER_NAME = "teedy-docker-container"      // Deployment 中的容器名称
        IMAGE_NAME = "iseeyourmonsters/teedy:3"  // 完整的 Docker 镜像地址
    }
    stages {
        stage('Start Minikube') {
            steps {
                sh '''
                    if ! minikube status | grep -q "Running"; then
                        echo "Starting Minikube..."
                        minikube start
                    else
                        echo "Minikube already running."
                    fi
                '''
            }
        }
        stage('Set Image') {
            steps {
                sh '''
                    echo "Setting image for deployment..."
                    kubectl set image deployment/${DEPLOYMENT_NAME} ${CONTAINER_NAME}=${IMAGE_NAME}
                '''
            }
        }
        stage('Verify') {
            steps {
                sh 'kubectl rollout status deployment/${DEPLOYMENT_NAME}'
                sh 'kubectl get pods'
            }
        }
    }
}
