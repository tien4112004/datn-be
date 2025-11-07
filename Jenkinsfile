pipeline {
    agent any

    // Environment variables
    environment {
        GITHUB_REPO = 'tien4112004/datn-be'
        DOCKER_REGISTRY = 'ghcr.io'
        IMAGE_NAME = "${DOCKER_REGISTRY}/${GITHUB_REPO}"
        DOCKER_COMPOSE_FILE = 'docker-compose.prod.yml'
        DEPLOY_DIR = '/opt/datn-be'
        ENV_FILE = '/opt/datn-be/.env.prod'
        CONTAINER_NAME = 'aiprimary-be'
        // GitHub integration
        GH_TOKEN = credentials('github-token')
        GH_API_URL = 'https://api.github.com'
    }

    options {
        // Keep builds for 30 days
        buildDiscarder(logRotator(numToKeepStr: '30', daysToKeepStr: '30'))
        // Add timestamps to console output
        timestamps()
        // Timeout after 1 hour
        timeout(time: 1, unit: 'HOURS')
    }

    triggers {
        // Trigger on webhook from GitHub (optional)
        githubPush()
    }

    stages {
        stage('Preparation') {
            steps {
                script {
                    echo "========== Deployment Preparation =========="
                    echo "Image: ${IMAGE_NAME}"
                    echo "Deploy Directory: ${DEPLOY_DIR}"
                    echo "Environment File: ${ENV_FILE}"
                    
                    // Clean workspace
                    cleanWs()
                    
                    // Create deploy directory if not exists
                    sh '''
                        mkdir -p ${DEPLOY_DIR}
                    '''
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    echo "========== Cloning Repository =========="
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[
                            url: "https://github.com/${GITHUB_REPO}.git",
                            credentialsId: 'github-credentials' // Make sure to configure this in Jenkins
                        ]]
                    ])
                    
                    // Show commit info
                    sh '''
                        echo "Commit Hash: $(git rev-parse HEAD)"
                        echo "Commit Message: $(git log -1 --pretty=%B)"
                        echo "Branch: $(git rev-parse --abbrev-ref HEAD)"
                    '''
                }
            }
        }

        stage('Update GitHub Status - Deploying') {
            steps {
                script {
                    echo "========== Updating GitHub Deployment Status =========="
                    
                    sh '''
                        COMMIT_SHA=$(git rev-parse HEAD)
                        
                        # Update GitHub commit status to pending
                        curl -X POST \
                            -H "Accept: application/vnd.github+json" \
                            -H "Authorization: token ${GH_TOKEN}" \
                            -H "X-GitHub-Api-Version: 2022-11-28" \
                            "${GH_API_URL}/repos/${GITHUB_REPO}/statuses/${COMMIT_SHA}" \
                            -d "{\"state\":\"pending\",\"target_url\":\"${BUILD_URL}\",\"description\":\"Deployment in progress on Jenkins\",\"context\":\"Jenkins Deployment\"}"
                        
                        echo "✓ GitHub status updated: pending"
                    '''
                }
            }
        }

        stage('Validate Environment') {
            steps {
                script {
                    echo "========== Validating Environment =========="
                    
                    // Check if Docker and Docker Compose are available
                    sh '''
                        docker --version
                        docker compose version
                        
                        # Check if environment file exists
                        if [ ! -f "${ENV_FILE}" ]; then
                            echo "WARNING: Environment file not found at ${ENV_FILE}"
                            echo "You must create it with required environment variables:"
                            echo "- KEYCLOAK_CLIENT_ID"
                            echo "- KEYCLOAK_CLIENT_SECRET"
                            echo "- KEYCLOAK_REDIRECT_URI"
                            echo "- KEYCLOAK_ISSUER_URI"
                            echo "- KEYCLOAK_REALM_NAME"
                            echo "- KEYCLOAK_SERVER_URL"
                            echo "- ALLOWED_ORIGINS"
                            echo "- R2_ACCOUNT_ID"
                            echo "- R2_BUCKET_NAME"
                            echo "- R2_PUBLIC_URL_DEV"
                            echo "- CLOUD_FLARE_API_TOKEN"
                            echo "- S3_ACCESS_KEY_ID"
                            echo "- S3_SECRET_ACCESS_KEY"
                            echo "- AI_API_BASE_URL"
                            echo "- AI_API_PRESENTATION_ENDPOINT"
                            echo "- AI_API_OUTLINE_ENDPOINT"
                        fi
                    '''
                }
            }
        }

        stage('Authenticate Docker Registry') {
            steps {
                script {
                    echo "========== Authenticating with GHCR =========="
                    
                    withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                        sh '''
                            echo "${GITHUB_TOKEN}" | docker login ${DOCKER_REGISTRY} -u ${GITHUB_USERNAME} --password-stdin
                        '''
                    }
                }
            }
        }

        stage('Pull Latest Image') {
            steps {
                script {
                    echo "========== Pulling Latest Docker Image =========="
                    
                    sh '''
                        docker pull ${IMAGE_NAME}:latest || true
                        docker pull ${IMAGE_NAME}:main || true
                        
                        # Show image info
                        docker image inspect ${IMAGE_NAME}:latest 2>/dev/null || echo "Image not found locally"
                    '''
                }
            }
        }

        stage('Stop Current Deployment') {
            steps {
                script {
                    echo "========== Stopping Current Deployment =========="
                    
                    sh '''
                        cd ${DEPLOY_DIR}
                        
                        if [ -f "${DOCKER_COMPOSE_FILE}" ]; then
                            docker compose -f ${DOCKER_COMPOSE_FILE} down --remove-orphans || true
                            echo "Containers stopped successfully"
                        else
                            echo "Docker Compose file not found in deploy directory"
                        fi
                    '''
                }
            }
        }

        stage('Copy Configuration') {
            steps {
                script {
                    echo "========== Copying Configuration Files =========="
                    
                    sh '''
                        # Copy docker-compose.prod.yml to deploy directory
                        cp ${WORKSPACE}/${DOCKER_COMPOSE_FILE} ${DEPLOY_DIR}/
                        
                        # Copy any additional config files if needed
                        # cp ${WORKSPACE}/config/* ${DEPLOY_DIR}/config/ 2>/dev/null || true
                        
                        echo "Configuration copied to ${DEPLOY_DIR}"
                        ls -la ${DEPLOY_DIR}
                    '''
                }
            }
        }

        stage('Start Deployment') {
            steps {
                script {
                    echo "========== Starting Deployment with Docker Compose =========="
                    
                    sh '''
                        cd ${DEPLOY_DIR}
                        
                        # Set proper image tag
                        export DOCKER_IMAGE="${IMAGE_NAME}:latest"
                        
                        # Pull and start containers
                        docker compose -f ${DOCKER_COMPOSE_FILE} --env-file ${ENV_FILE} pull
                        docker compose -f ${DOCKER_COMPOSE_FILE} --env-file ${ENV_FILE} up -d
                        
                        echo "Containers started successfully"
                    '''
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo "========== Performing Health Check =========="
                    
                    sh '''
                        # Wait for container to be healthy
                        sleep 10
                        
                        # Check if container is running
                        if docker ps | grep -q ${CONTAINER_NAME}; then
                            echo "✓ Container ${CONTAINER_NAME} is running"
                        else
                            echo "✗ Container ${CONTAINER_NAME} is NOT running"
                            docker ps -a
                            exit 1
                        fi
                        
                        # Show container logs (last 20 lines)
                        echo "========== Recent Container Logs =========="
                        docker logs --tail 20 ${CONTAINER_NAME}
                        
                        # Check if application is responding
                        sleep 5
                        if docker exec ${CONTAINER_NAME} curl -f http://localhost:8080/actuator/health &>/dev/null; then
                            echo "✓ Application health check passed"
                        else
                            echo "⚠ Health check endpoint not responding yet (this may be normal during startup)"
                        fi
                    '''
                }
            }
        }

        stage('Cleanup Old Images') {
            steps {
                script {
                    echo "========== Cleaning Up Old Docker Images =========="
                    
                    sh '''
                        # Remove dangling images
                        docker image prune -f || true
                        
                        # Remove unused volumes
                        docker volume prune -f || true
                        
                        # Show disk usage
                        docker system df
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo "========== Pipeline Completed =========="
                
                // Save deployment logs
                sh '''
                    mkdir -p ${WORKSPACE}/logs
                    docker logs ${CONTAINER_NAME} > ${WORKSPACE}/logs/deployment.log 2>&1 || true
                    docker compose -f ${DEPLOY_DIR}/${DOCKER_COMPOSE_FILE} ps > ${WORKSPACE}/logs/containers.log 2>&1 || true
                '''
            }
        }

        success {
            script {
                echo "✓ Deployment successful!"
                
                // Update GitHub commit status to success
                sh '''
                    COMMIT_SHA=$(git rev-parse HEAD)
                    
                    curl -X POST \
                        -H "Accept: application/vnd.github+json" \
                        -H "Authorization: token ${GH_TOKEN}" \
                        -H "X-GitHub-Api-Version: 2022-11-28" \
                        "${GH_API_URL}/repos/${GITHUB_REPO}/statuses/${COMMIT_SHA}" \
                        -d "{\"state\":\"success\",\"target_url\":\"${BUILD_URL}\",\"description\":\"Deployment successful on Jenkins\",\"context\":\"Jenkins Deployment\"}"
                    
                    echo "✓ GitHub status updated: success"
                '''
                
                // Optional: Send success notification
                // emailext(
                //     subject: "Deployment Successful - datn-be",
                //     body: "The application has been successfully deployed.\n\nCommit: ${GIT_COMMIT}\n\nBuild: ${BUILD_URL}",
                //     to: "your-email@example.com"
                // )
            }
        }

        failure {
            script {
                echo "✗ Deployment failed!"
                
                // Update GitHub commit status to failure
                sh '''
                    COMMIT_SHA=$(git rev-parse HEAD)
                    
                    curl -X POST \
                        -H "Accept: application/vnd.github+json" \
                        -H "Authorization: token ${GH_TOKEN}" \
                        -H "X-GitHub-Api-Version: 2022-11-28" \
                        "${GH_API_URL}/repos/${GITHUB_REPO}/statuses/${COMMIT_SHA}" \
                        -d "{\"state\":\"failure\",\"target_url\":\"${BUILD_URL}\",\"description\":\"Deployment failed on Jenkins\",\"context\":\"Jenkins Deployment\"}"
                    
                    echo "✓ GitHub status updated: failure"
                '''
                
                // Print detailed logs for debugging
                sh '''
                    echo "========== Container Status =========="
                    docker ps -a
                    
                    echo "========== Recent Logs =========="
                    docker logs --tail 50 ${CONTAINER_NAME} || true
                    
                    echo "========== Docker Compose Status =========="
                    cd ${DEPLOY_DIR}
                    docker compose -f ${DOCKER_COMPOSE_FILE} ps || true
                '''
                
                // Optional: Send failure notification
                // emailext(
                //     subject: "Deployment Failed - datn-be",
                //     body: "The deployment has failed.\n\nCommit: ${GIT_COMMIT}\n\nBuild: ${BUILD_URL}\n\nPlease check the logs for details.",
                //     to: "your-email@example.com"
                // )
            }
        }

        unstable {
            echo "⚠ Pipeline is unstable"
        }

        cleanup {
            script {
                echo "Cleaning up workspace..."
                // Optional: Clean workspace after build
                // cleanWs()
            }
        }
    }
}
