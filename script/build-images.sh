#!/usr/bin/env bash
set -euo pipefail

IMAGE_PREFIX="datn"
IMAGE_TAG="latest"

BUSINESS_SERVICES=(presentation auth api-gateway)
EXCLUDE_DIRS=(script)

# Parse command line arguments: --business-only or -b
BUSINESS_ONLY=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    -b|--business-only)
      BUSINESS_ONLY=true
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 [--business-only|-b]"
      exit 1
      ;;
  esac
done

# Get absolute path to repo root (where this script lives)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "================== Build Jar files for all services in ${ROOT_DIR} =================="
echo

mvn clean package -DskipTests -PbuildDocker

echo "================== Building Docker images from all service folders under ${ROOT_DIR} =================="
echo

for service_dir in "${ROOT_DIR}"/*/; do
  service_name="$(basename "${service_dir}")"

  if [[ " ${EXCLUDE_DIRS[*]} " == *" ${service_name} "* ]]; then
    echo "================== Skipping excluded folder: ${service_name} =================="
    continue
  fi

  # In business-only mode, skip non-business services
  if [[ "${BUSINESS_ONLY,,}" == "true" && ! " ${BUSINESS_SERVICES[*]} " == *" ${service_name} "* ]]; then
    echo "================== Skipping non-business service: ${service_name} =================="
    continue
  fi

  # Check for Dockerfile in each folder
  if [ -f "${service_dir}Dockerfile" ]; then
    service_name="$(basename "${service_dir}")"
    image_name="${IMAGE_PREFIX}/${service_name}:${IMAGE_TAG}"
    echo "================== Building ${image_name} from ${service_dir} =================="
    docker build \
      --file "${service_dir}Dockerfile" \
      --tag "${image_name}" \
      "${service_dir}"
    echo "================== Built ${image_name} =================="
    echo
  fi
done

echo "================== All done. =================="
