#!/bin/bash

set -e

echo "========================================="
echo "Model Configuration Initialization Script"
echo "========================================="

# Wait for database to be ready
echo "Waiting for database to be ready..."
max_retries=30
retry_count=0

until PGPASSWORD=$POSTGRES_DB_PASSWORD psql -h "$(echo $POSTGRES_DB_URL | sed -n 's/.*\/\/\([^:]*\):.*/\1/p')" -U "$POSTGRES_DB_USERNAME" -d "$(echo $POSTGRES_DB_URL | sed -n 's/.*\/\([^?]*\).*/\1/p')" -c '\q' 2>/dev/null; do
  retry_count=$((retry_count + 1))
  if [ $retry_count -ge $max_retries ]; then
    echo "ERROR: Database is not available after $max_retries attempts"
    exit 1
  fi
  echo "Database is unavailable - waiting... (attempt $retry_count/$max_retries)"
  sleep 2
done

echo "Database is ready!"

# Run the application in init-only mode
echo "Running model configuration initialization..."
java $JAVA_OPTS \
  -Dspring.main.web-application-type=none \
  -Dapp.init.enabled=true \
  -Dapp.init.exit-after-init=true \
  org.springframework.boot.loader.launch.JarLauncher

if [ $? -eq 0 ]; then
  echo "Model configuration initialization completed successfully!"
else
  echo "ERROR: Model configuration initialization failed!"
  exit 1
fi

echo "========================================="
