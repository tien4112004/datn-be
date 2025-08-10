#!/usr/bin/env bash
set -euo pipefail

IMAGE_PREFIX="datn"
IMAGE_TAG="latest"

SERVICE_TO_BUILD="$1"

# Get absolute path to repo root (where this script lives)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "================== Build Jar file for service ${SERVICE_TO_BUILD} in ${ROOT_DIR} =================="
echo

mvn clean package -DskipTests -PbuildDocker

echo "================== Building Docker image for service ${SERVICE_TO_BUILD} =================="
echo    

service_dir="${ROOT_DIR}/${SERVICE_TO_BUILD}/"

if [ -d "${service_dir}" ]; then
  if [ -f "${service_dir}Dockerfile" ]; then
    image_name="${IMAGE_PREFIX}/${SERVICE_TO_BUILD}:${IMAGE_TAG}"
    echo "================== Building ${image_name} from ${service_dir} =================="
    docker build \
      --file "${service_dir}Dockerfile" \
      --tag "${image_name}" \
      "${service_dir}"
  else
    echo "Error: Dockerfile not found in ${service_dir}"
    exit 1
  fi
else
  echo "Error: Service directory ${service_dir} does not exist"
  exit 1
fi

echo "================== Successfully built ${image_name} =================="