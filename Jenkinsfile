pipeline {
    agent any

    // Environment variables
    environment {
        GITHUB_REPO = 'tien4112004/datn-be'
        GITHUB_USERNAME = 'tien4112004'  // GHCR
        DOCKER_REGISTRY = 'ghcr.io'
        IMAGE_NAME = "${DOCKER_REGISTRY}/${GITHUB_REPO}"
        DOCKER_COMPOSE_DB_FILE = 'docker-compose.db.prod.yml'
        DOCKER_COMPOSE_APP_FILE = 'docker-compose.prod.yml'
        DEPLOY_DIR = '/opt/datn-be'
        ENV_FILE = '/opt/datn-be/.env.prod'
        CONTAINER_NAME = 'backend-aiprimary'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', daysToKeepStr: '30'))
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }

    stages {
        stage('Preparation') {
            steps {
                script {
                    echo "========== Deployment Preparation =========="
                    echo "Image: ${IMAGE_NAME}"
                    echo "Deploy Directory: ${DEPLOY_DIR}"
                    echo "Environment File: ${ENV_FILE}"
                    echo "Branch: ${env.BRANCH_NAME}"
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
                    
                    withCredentials([string(credentialsId: 'github_pat_username', variable: 'GITHUB_TOKEN')]) {
                        sh '''
                            # Validate token is not empty
                            if [ -z "${GITHUB_TOKEN}" ]; then
                                echo "ERROR: GITHUB_TOKEN is empty"
                                exit 1
                            fi
                            
                            # Set username explicitly
                            GHCR_USERNAME="tien4112004"
                            
                            # Login to GHCR
                            echo "${GITHUB_TOKEN}" | docker login ghcr.io -u "${GHCR_USERNAME}" --password-stdin
                            
                            echo "✓ Successfully authenticated to ghcr.io"
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
                    echo "========== Stopping Current Backend Deployment =========="
                    
                    sh '''
                        cd ${DEPLOY_DIR}
                        
                        # Only stop the backend application, not the databases
                        if [ -f "${DOCKER_COMPOSE_APP_FILE}" ]; then
                            docker compose -f ${DOCKER_COMPOSE_APP_FILE} down --remove-orphans || true
                            echo "Backend containers stopped successfully"
                        else
                            echo "Backend Docker Compose file not found in deploy directory"
                        fi
                        
                        # Note: Database services (${DOCKER_COMPOSE_DB_FILE}) are kept running
                    '''
                }
            }
        }

        stage('Copy Configuration') {
            steps {
                script {
                    echo "========== Copying Configuration Files =========="
                    
                    sh '''
                        # Copy both docker-compose files to deploy directory
                        cp ${WORKSPACE}/${DOCKER_COMPOSE_DB_FILE} ${DEPLOY_DIR}/
                        cp ${WORKSPACE}/${DOCKER_COMPOSE_APP_FILE} ${DEPLOY_DIR}/
                        
                        # Copy any additional config files if needed
                        # cp ${WORKSPACE}/config/* ${DEPLOY_DIR}/config/ 2>/dev/null || true
                        
                        echo "Configuration copied to ${DEPLOY_DIR}"
                        echo "Files in deploy directory:"
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
                        
                        # Step 1: Ensure database services are running (if not already)
                        echo "Checking database services..."
                        if [ -f "${DOCKER_COMPOSE_DB_FILE}" ]; then
                            # Start database services (this is idempotent - won't recreate if already running)
                            docker compose -f ${DOCKER_COMPOSE_DB_FILE} up -d
                            echo "Database services are starting..."
                            
                            # Wait for databases to be healthy
                            echo "Waiting for database services to be healthy..."
                            
                            # Wait for PostgreSQL
                            echo "Checking PostgreSQL..."
                            timeout=60
                            counter=0
                            until docker exec postgres-aiprimary pg_isready -U postgres > /dev/null 2>&1; do
                                counter=$((counter + 1))
                                if [ $counter -gt $timeout ]; then
                                    echo "ERROR: PostgreSQL failed to become ready within ${timeout} seconds"
                                    exit 1
                                fi
                                echo "Waiting for PostgreSQL... ($counter/$timeout)"
                                sleep 1
                            done
                            echo "✓ PostgreSQL is ready"
                            
                            # Wait for MongoDB
                            echo "Checking MongoDB..."
                            counter=0
                            until docker exec mongodb-aiprimary mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; do
                                counter=$((counter + 1))
                                if [ $counter -gt $timeout ]; then
                                    echo "ERROR: MongoDB failed to become ready within ${timeout} seconds"
                                    exit 1
                                fi
                                echo "Waiting for MongoDB... ($counter/$timeout)"
                                sleep 1
                            done
                            echo "✓ MongoDB is ready"
                            
                            # Wait for Keycloak
                            echo "Checking Keycloak..."
                            counter=0
                            until docker exec keycloak-aiprimary curl -sf http://localhost:8080/health/ready > /dev/null 2>&1; do
                                counter=$((counter + 1))
                                if [ $counter -gt $timeout ]; then
                                    echo "ERROR: Keycloak failed to become ready within ${timeout} seconds"
                                    exit 1
                                fi
                                echo "Waiting for Keycloak... ($counter/$timeout)"
                                sleep 1
                            done
                            echo "✓ Keycloak is ready"
                            
                            echo "✓ All database services are healthy"
                        else
                            echo "WARNING: Database compose file not found at ${DEPLOY_DIR}/${DOCKER_COMPOSE_DB_FILE}"
                            echo "Please ensure database services are running separately"
                        fi
                        
                        # Step 2: Pull and start backend application
                        echo "Starting backend application..."
                        export DOCKER_IMAGE="${IMAGE_NAME}:latest"
                        
                        docker compose -f ${DOCKER_COMPOSE_APP_FILE} --env-file ${ENV_FILE} pull
                        docker compose -f ${DOCKER_COMPOSE_APP_FILE} --env-file ${ENV_FILE} up -d
                        
                        echo "Backend containers started successfully"
                        
                        # Wait a bit for backend to initialize
                        echo "Waiting for backend to initialize..."
                        sleep 10
                        
                        # Show status of all services
                        echo "========== Service Status =========="
                        echo "Database Services:"
                        docker compose -f ${DOCKER_COMPOSE_DB_FILE} ps 2>/dev/null || echo "No database compose file"
                        echo ""
                        echo "Backend Services:"
                        docker compose -f ${DOCKER_COMPOSE_APP_FILE} ps
                    '''
                }
            }
        }

        stage('Cleanup Old Images') {
            when {
                branch 'main'
            }
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
                
                // Save deployment logs only if container exists
                sh '''
                    mkdir -p ${WORKSPACE}/logs || true
                    
                    # Only save logs if container exists
                    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
                        echo "Saving logs for container ${CONTAINER_NAME}..."
                        docker logs ${CONTAINER_NAME} > ${WORKSPACE}/logs/deployment.log 2>&1 || true
                    else
                        echo "Container ${CONTAINER_NAME} does not exist, skipping log collection"
                    fi
                    
                    # Only save compose status if compose file exists
                    if [ -f "${DEPLOY_DIR}/${DOCKER_COMPOSE_APP_FILE}" ]; then
                        docker compose -f ${DEPLOY_DIR}/${DOCKER_COMPOSE_APP_FILE} ps > ${WORKSPACE}/logs/containers.log 2>&1 || true
                    fi
                    
                    # Also save database status if available
                    if [ -f "${DEPLOY_DIR}/${DOCKER_COMPOSE_DB_FILE}" ]; then
                        docker compose -f ${DEPLOY_DIR}/${DOCKER_COMPOSE_DB_FILE} ps >> ${WORKSPACE}/logs/containers.log 2>&1 || true
                    fi
                '''
            }
        }

        success {
            script {
                echo "✓ Deployment successful!"
            }
        }

        failure {
            script {
                echo "✗ Deployment failed!"
                
                sh '''
                    echo "========== Container Status =========="
                    docker ps -a || true
                    
                    echo "========== Recent Logs =========="
                    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
                        docker logs --tail 50 ${CONTAINER_NAME} || true
                    else
                        echo "Container ${CONTAINER_NAME} does not exist yet"
                    fi
                    
                    echo "========== Docker Compose Status =========="
                    if [ -f "${DEPLOY_DIR}/${DOCKER_COMPOSE_APP_FILE}" ]; then
                        cd ${DEPLOY_DIR}
                        echo "Backend Status:"
                        docker compose -f ${DOCKER_COMPOSE_APP_FILE} ps || true
                        echo ""
                        echo "Database Status:"
                        docker compose -f ${DOCKER_COMPOSE_DB_FILE} ps || true
                    else
                        echo "Docker compose files not found at ${DEPLOY_DIR}"
                    fi
                '''
            }
        }

        unstable {
            echo "⚠ Pipeline is unstable"
        }

        cleanup {
            script {
                echo "Cleaning up workspace..."
                // cleanWs()
            }
        }
    }
}
