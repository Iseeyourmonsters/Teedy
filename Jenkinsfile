pipeline {
    agent any
    environment {
    // ✅ 正确填写以下三个变量 ✅
    DEPLOYMENT_NAME = "hello-node"    // 使用实际存在的 Deployment 名称
    CONTAINER_NAME = "docs"           // 容器名称从 Deployment 描述中获取
    IMAGE_NAME = "iseeyourmonsters/teedy:3"  // 你的新镜像地址（确保已推送到仓库）
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
