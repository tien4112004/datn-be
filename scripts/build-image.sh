#!/usr/bin/env bash
set -euo pipefail

# Docker image build script for DATN Backend
# This script builds a single Docker image for the Spring Boot application

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
IMAGE_NAME="datn-be"
DEFAULT_TAG="latest"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Build Docker image for DATN Backend Spring Boot application

OPTIONS:
    -t, --tag TAG       Docker image tag (default: $DEFAULT_TAG)
    -n, --name NAME     Docker image name (default: $IMAGE_NAME)
    -p, --push          Push image to registry after build
    -c, --clean         Clean build (remove intermediate containers)
    --no-cache          Build without using cache
    --platform PLATFORM Target platform (e.g., linux/amd64,linux/arm64)
    -h, --help          Show this help message

EXAMPLES:
    $0                              # Build with default settings
    $0 -t v1.0.0                   # Build with specific tag
    $0 -t latest -p                # Build and push to registry
    $0 --no-cache -c               # Clean build without cache
    $0 --platform linux/amd64     # Build for specific platform

ENVIRONMENT VARIABLES:
    DOCKER_REGISTRY     Docker registry URL (for push operation)
    DOCKER_USERNAME     Docker registry username
    DOCKER_PASSWORD     Docker registry password/token
EOF
}

# Parse command line arguments
TAG="$DEFAULT_TAG"
PUSH=false
CLEAN=false
NO_CACHE=false
PLATFORM=""

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--tag)
            TAG="$2"
            shift 2
            ;;
        -n|--name)
            IMAGE_NAME="$2"
            shift 2
            ;;
        -p|--push)
            PUSH=true
            shift
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        --no-cache)
            NO_CACHE=true
            shift
            ;;
        --platform)
            PLATFORM="$2"
            shift 2
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate Docker installation
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

# Change to project root directory
cd "$PROJECT_ROOT"

print_info "Starting Docker image build for DATN Backend"
print_info "Project root: $PROJECT_ROOT"
print_info "Image name: $IMAGE_NAME"
print_info "Tag: $TAG"

# Clean up if requested
if [ "$CLEAN" = true ]; then
    print_info "Cleaning up dangling images and build cache..."
    docker image prune -f || true
    docker builder prune -f || true
fi

# Build Docker build command
BUILD_CMD="docker build"

export DOCKER_BUILDKIT=1

# Add platform if specified
if [ -n "$PLATFORM" ]; then
    BUILD_CMD="$BUILD_CMD --platform $PLATFORM"
fi

# Add no-cache option if specified
if [ "$NO_CACHE" = true ]; then
    BUILD_CMD="$BUILD_CMD --no-cache"
fi

# Add build arguments
BUILD_CMD="$BUILD_CMD --build-arg BUILDKIT_INLINE_CACHE=1"

# Add tags
FULL_IMAGE_NAME="$IMAGE_NAME:$TAG"
BUILD_CMD="$BUILD_CMD -t $FULL_IMAGE_NAME"

# Add latest tag if not building latest
if [ "$TAG" != "latest" ]; then
    BUILD_CMD="$BUILD_CMD -t $IMAGE_NAME:latest"
fi

# Add Dockerfile and context
BUILD_CMD="$BUILD_CMD -f Dockerfile ."

print_info "Building Docker image..."
print_info "Command: $BUILD_CMD"

# Execute build command
if eval "$BUILD_CMD"; then
    print_success "Docker image built successfully: $FULL_IMAGE_NAME"
    
    # Show image details
    print_info "Image details:"
    docker images "$IMAGE_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}\t{{.CreatedAt}}"
else
    print_error "Docker image build failed"
    exit 1
fi

# Push to registry if requested
if [ "$PUSH" = true ]; then
    print_info "Pushing image to registry..."
    
    # Add registry prefix if DOCKER_REGISTRY is set
    if [ -n "${DOCKER_REGISTRY:-}" ]; then
        REGISTRY_IMAGE="$DOCKER_REGISTRY/$FULL_IMAGE_NAME"
        print_info "Tagging image for registry: $REGISTRY_IMAGE"
        docker tag "$FULL_IMAGE_NAME" "$REGISTRY_IMAGE"
        PUSH_IMAGE="$REGISTRY_IMAGE"
    else
        PUSH_IMAGE="$FULL_IMAGE_NAME"
    fi
    
    # Login to registry if credentials are provided
    if [ -n "${DOCKER_USERNAME:-}" ] && [ -n "${DOCKER_PASSWORD:-}" ]; then
        print_info "Logging in to Docker registry..."
        echo "$DOCKER_PASSWORD" | docker login "${DOCKER_REGISTRY:-}" --username "$DOCKER_USERNAME" --password-stdin
    fi
    
    # Push the image
    if docker push "$PUSH_IMAGE"; then
        print_success "Image pushed successfully: $PUSH_IMAGE"
    else
        print_error "Failed to push image: $PUSH_IMAGE"
        exit 1
    fi
    
    # Also push latest tag if we tagged it
    if [ "$TAG" != "latest" ] && [ -n "${DOCKER_REGISTRY:-}" ]; then
        LATEST_REGISTRY_IMAGE="$DOCKER_REGISTRY/$IMAGE_NAME:latest"
        docker tag "$IMAGE_NAME:latest" "$LATEST_REGISTRY_IMAGE"
        if docker push "$LATEST_REGISTRY_IMAGE"; then
            print_success "Latest tag pushed successfully: $LATEST_REGISTRY_IMAGE"
        else
            print_warning "Failed to push latest tag: $LATEST_REGISTRY_IMAGE"
        fi
    fi
fi

# Clean up temporary tags if registry was used
if [ "$PUSH" = true ] && [ -n "${DOCKER_REGISTRY:-}" ]; then
    print_info "Cleaning up registry tags..."
    docker rmi "$DOCKER_REGISTRY/$FULL_IMAGE_NAME" 2>/dev/null || true
    if [ "$TAG" != "latest" ]; then
        docker rmi "$DOCKER_REGISTRY/$IMAGE_NAME:latest" 2>/dev/null || true
    fi
fi

print_success "Build process completed successfully!"
print_info "You can now run the application with:"
print_info "  docker run -p 8080:8080 $FULL_IMAGE_NAME"
print_info ""
print_info "Or use docker-compose:"
print_info "  docker-compose up"
