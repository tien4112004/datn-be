## DATN Backend

A Spring Boot modular monolith backend for the DATN project, built with Spring Modulith, featuring authentication, document management, AI services, and presentation functionality in a single deployable application.

## Table of Contents

- [DATN Backend](#datn-backend)
- [Table of Contents](#table-of-contents)
  - [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Configure Environment Variables](#2-configure-environment-variables)
  - [3. Install Git Hooks \& Dependencies](#3-install-git-hooks--dependencies)
  - [4. Running the Application](#4-running-the-application)
    - [a. Local Development](#a-local-development)
    - [b. With Docker](#b-with-docker)
- [Build \& Deployment](#build--deployment)
  - [Gradle Build System](#gradle-build-system)
  - [Docker Build](#docker-build)
- [Database Configuration](#database-configuration)
  - [1. Fully Docker Setup (Recommended)](#1-fully-docker-setup-recommended)
  - [2. Local Database Setup](#2-local-database-setup)
- [Testing](#testing)
  - [Test Structure](#test-structure)
  - [Running Tests](#running-tests)
    - [Run All Tests](#run-all-tests)
    - [Run Tests by Module](#run-tests-by-module)
    - [Run Specific Test Classes](#run-specific-test-classes)
    - [Run Specific Test Methods](#run-specific-test-methods)
    - [Run Tests by Pattern](#run-tests-by-pattern)
    - [Test Execution Options](#test-execution-options)
    - [Test Reports and Output](#test-reports-and-output)
    - [Environment-Specific Testing](#environment-specific-testing)
    - [Common Test Scenarios](#common-test-scenarios)
  - [Test Best Practices](#test-best-practices)
  - [Debugging Tests](#debugging-tests)
- [Modules](#modules)
- [Scripts](#scripts)
  - [`install.sh`](#installsh)
  - [`build-image.sh`](#build-imagesh)
- [Git Hooks](#git-hooks)
  - [Available Hooks](#available-hooks)
- [Directory Structure](#directory-structure)

---

### Prerequisites

Ensure the following are installed on your development machine:

- **Git**: [https://git-scm.com/](https://git-scm.com/)
- **Docker & Docker Compose**: [https://docs.docker.com/](https://docs.docker.com/)
- **Java 21** (or later): [https://adoptium.net/](https://adoptium.net/)
- **Gradle**: [https://gradle.org/](https://gradle.org/) (or use the included Gradle wrapper)
- **Node.js** (for commitlint): [https://nodejs.org/](https://nodejs.org/)
- **curl** (for health checks): Usually pre-installed on most systems

---

## Getting Started

Follow these steps to get the backend up and running.

### 1. Clone the Repository

```bash
git clone https://github.com/tien4112004/datn-be.git
cd datn-be
```

### 2. Configure Environment Variables

1. Copy the sample environment file:

```bash
cp .env.sample .env
```

2. Update `.env` based on your setup:

    - **For fully Docker setup**: No database environment variables needed (uses Docker databases)
    - **For local database setup**: Set database environment variables (
      see [Database Configuration](#database-configuration))

> **Note:**
>
> - The installer script (`scripts/install.sh`) automatically loads `.env`.
> - If you skip the installer, load variables manually:
    >
    >   ```bash
>   source .env
>   ```

### 3. Install Git Hooks & Dependencies

Make the installer executable and run it:

```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

This script will:

- Reload environment variables
- Install Git hooks (View [Git Hooks](#git-hooks) section)

### 4. Running the Application

#### a. Local Development

1. Ensure your local database URLs are correctly set in `.env` (see [Database Configuration](#database-configuration) section).

2. Start the Spring Boot application using Gradle:

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

Or run with specific profiles:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### b. With Docker

**Option 1: Build and run manually**

```bash
# Build the Docker image
docker build -t datn-be:latest .

# Run with databases (start databases first)
docker-compose -f docker-compose.db.yml up -d

# Run the application container
docker run -p 8080:8080 \
  --network datn-be_default \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e POSTGRES_DB_URL=jdbc:postgresql://postgres-monolith:5432/datn_monolith_db \
  -e POSTGRES_DB_USERNAME=postgres \
  -e POSTGRES_DB_PASSWORD=postgres \
  -v ${VERTEX_SERVICE_ACCOUNT_KEY_PATH}:/secrets/key.json:ro \
  datn-be:latest
```

**Option 2: Using Docker Compose (Recommended)**

```bash
# Start databases only
docker-compose -f docker-compose.db.yml up -d

# Start the complete stack (databases + application)
docker-compose up -d

# Or start everything at once
docker-compose -f docker-compose.db.yml -f docker-compose.yml up -d
```

**Option 3: Using build script**

```bash
# Build image with the provided script
./scripts/build-image.sh

# Build with specific tag
./scripts/build-image.sh -t v1.0.0

# Build and push to registry
./scripts/build-image.sh -t latest -p
```

---

## Build & Deployment

### Gradle Build System

The project uses Gradle with the following key features:

- **Java 21 Toolchain**: Ensures consistent Java version across environments
- **Spring Boot 3.5.4**: Latest stable Spring Boot version
- **Spring Modulith**: For modular monolith architecture
- **Multi-layered Dependencies**: AI services, data persistence, validation, and monitoring

**Key Gradle Commands:**

```bash
# Clean and build the project
./gradlew clean build

# Run the application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run tests
./gradlew test

# Build without running tests
./gradlew build -x test

# Generate dependency insight
./gradlew dependencyInsight --dependency spring-boot-starter-web
```

### Docker Build

The application uses a **multi-stage Docker build** for optimal image size and security:

**Build Stages:**
1. **Builder Stage** (`eclipse-temurin:21`):
   - Copies Gradle wrapper and build files
   - Builds the application using `./gradlew clean build -x test`
   - Extracts Spring Boot layers using `jarmode=layertools`

2. **Runtime Stage** (`eclipse-temurin:21-jre`):
   - Uses JRE-only image for smaller footprint
   - Creates non-root user for security
   - Copies extracted layers for better Docker layer caching
   - Includes health check and optimized JVM settings

**Docker Features:**
- **Layer-based extraction**: Better caching and faster subsequent builds
- **Non-root user**: Enhanced security
- **Health checks**: Built-in endpoint monitoring
- **JVM optimization**: Container-aware memory settings
- **Environment-specific profiles**: Docker profile activation

---

## Database Configuration

The application supports two database setup modes:

### 1. Fully Docker Setup (Recommended)

Uses containerized databases - **no database environment variables needed** in `.env`.

- PostgreSQL containers for all application data
- All connection details are handled automatically via Docker networking

To start with databases:

```bash
docker-compose -f docker-compose.db.yml -f docker-compose.yml up -d
```

### 2. Local Database Setup

For connecting to local or external databases, configure these variables in `.env`:

**PostgreSQL (Single Monolith Database):**

```bash
# Single PostgreSQL database for the monolith
POSTGRES_DB_URL=jdbc:postgresql://localhost:5432/datn_monolith_db
POSTGRES_DB_USERNAME=postgres
POSTGRES_DB_PASSWORD=postgres
```

**AI Services Configuration:**

```bash
# OpenAI API
OPENAI_KEY=your_openai_api_key

# Google Vertex AI
VERTEX_PROJECT_ID=your_gcp_project_id
VERTEX_LOCATION=us-central1
VERTEX_SERVICE_ACCOUNT_KEY_PATH=/path/to/service-account.json
```

**Other Configuration:**

```bash
# CORS settings
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# RabbitMQ (if needed)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

After updating `.env`, reload environment variables:

```bash
source .env
# or run the installer again
./scripts/install.sh
```

---

## Testing

The project includes comprehensive unit and integration tests using **JUnit 5**, **Mockito**, and **Spring Boot Test**. Tests are organized by module and follow best practices for testing Spring Boot applications.

### Test Structure

- **Unit Tests**: Test individual components in isolation using mocks
- **Integration Tests**: Test module interactions and database operations
- **Web Layer Tests**: Test REST controllers using `@WebMvcTest`
- **Service Layer Tests**: Test business logic with mocked dependencies

### Running Tests

#### Run All Tests

```bash
# Run all tests in the project
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run tests and generate coverage report
./gradlew test jacocoTestReport
```

#### Run Tests by Module

```bash
# Run all tests in the AI module
./gradlew test --tests "com.datn.datnbe.ai.*"

# Run all tests in the Auth module
./gradlew test --tests "com.datn.datnbe.auth.*"

# Run all tests in the Document module
./gradlew test --tests "com.datn.datnbe.document.*"

# Run all tests in the Shared Kernel module
./gradlew test --tests "com.datn.datnbe.sharedkernel.*"
```

#### Run Specific Test Classes

```bash
# Run a specific test class
./gradlew test --tests "ModelConfigurationControllerTest"

# Run multiple specific test classes
./gradlew test --tests "ModelConfigurationControllerTest" --tests "ModelSelectionManagementTest"

# Run test class with full package name
./gradlew test --tests "com.datn.datnbe.ai.presentation.ModelConfigurationControllerTest"
```

#### Run Specific Test Methods

```bash
# Run a specific test method
./gradlew test --tests "ModelConfigurationControllerTest.getTextModels_WithValidData_ShouldReturnSuccessResponse"

# Run multiple test methods from the same class
./gradlew test --tests "ModelConfigurationControllerTest.getTextModels*"

# Run all text model tests in ModelConfigurationController
./gradlew test --tests "ModelConfigurationControllerTest.getTextModels*" --tests "ModelConfigurationControllerTest.getImageModels*"
```

#### Run Tests by Pattern

```bash
# Run all controller tests
./gradlew test --tests "*Controller*"

# Run all service tests
./gradlew test --tests "*Service*" --tests "*Management*"

# Run all tests containing "Model" in the name
./gradlew test --tests "*Model*"

# Run all integration tests (assuming they end with IntegrationTest)
./gradlew test --tests "*IntegrationTest"
```

#### Test Execution Options

```bash
# Run tests in parallel (faster execution)
./gradlew test --parallel

# Run tests with maximum parallel workers
./gradlew test --max-workers=4

# Continue running tests even if some fail
./gradlew test --continue

# Run tests without build cache
./gradlew test --no-build-cache

# Run only failed tests from previous run
./gradlew test --rerun-tasks
```

#### Test Reports and Output

```bash
# Generate and open test report in browser (Linux/macOS)
./gradlew test && open build/reports/tests/test/index.html

# View test results in terminal with details
./gradlew test --info --stacktrace

# Run tests with debug information
./gradlew test --debug-jvm

# Generate test coverage report
./gradlew test jacocoTestReport
# Coverage report will be available at: build/reports/jacoco/test/html/index.html
```

#### Environment-Specific Testing

```bash
# Run tests with test profile
./gradlew test -Dspring.profiles.active=test

# Run tests with specific environment variables
./gradlew test -DTEST_DATABASE_URL=jdbc:h2:mem:testdb

# Run integration tests with Docker database
docker-compose -f docker-compose.db.yml up -d
./gradlew test --tests "*IntegrationTest"
```

#### Common Test Scenarios

```bash
# Quick smoke test (run fastest tests first)
./gradlew test --tests "*Unit*" --fail-fast

# Run only tests that changed since last commit
./gradlew test --continuous

# Test specific functionality
./gradlew test --tests "*ModelConfiguration*" --tests "*ModelSelection*"

# Run tests for a specific feature
./gradlew test --tests "*Auth*" --tests "*Login*" --tests "*Token*"

# Pre-commit testing (fast feedback loop)
./gradlew test --tests "*Unit*" --parallel --continue
```

### Test Best Practices

- **Test Naming**: Use descriptive names following the pattern `methodName_condition_expectedResult`
- **Test Organization**: Group tests by functionality using nested classes or clear section comments
- **Mock Usage**: Use `@MockBean` for Spring components and `@Mock` for regular objects
- **Test Data**: Use builders or factory methods for creating test data consistently
- **Assertions**: Use AssertJ for fluent and readable assertions
- **Coverage**: Aim for high test coverage but focus on critical business logic

### Debugging Tests

```bash
# Run a single test with debugging enabled
./gradlew test --tests "YourTestClass.yourTestMethod" --debug-jvm

# Run tests with verbose logging
./gradlew test --tests "YourTestClass" --info --stacktrace

# Run tests with specific log level
./gradlew test -Dlogging.level.com.datn.datnbe=DEBUG
```

---

## Modules

This application is built as a **modular monolith** using **Spring Modulith**. The main modules are:

- **Auth Module** (`com.datn.datnbe.auth`): Handles user authentication and authorization using PostgreSQL
- **Document Module** (`com.datn.datnbe.document`): Manages document storage, retrieval, and presentation generation using PostgreSQL JSONB  
- **AI Module** (`com.datn.datnbe.ai`): Provides AI-powered features using OpenAI GPT and Google Vertex AI Gemini
- **Shared Kernel** (`com.datn.datnbe.sharedkernel`): Common DTOs, events, exceptions, and cross-module utilities

**Module Architecture:**
Each module is self-contained with its own:
- **Domain models**: Business entities and value objects
- **Service layer**: Business logic and orchestration
- **Repository/Data access**: JPA repositories for PostgreSQL with JSONB support for complex types
- **REST controllers**: HTTP endpoints and request/response handling
- **Configuration**: Module-specific beans and settings
- **Events**: Inter-module communication via Spring Application Events

**Spring Modulith Benefits:**
- **Compile-time validation**: Ensures proper module boundaries
- **Documentation generation**: Automatic module documentation
- **Integration testing**: Module slice testing capabilities
- **Event-driven architecture**: Async communication between modules
- **Observability**: Built-in monitoring and tracing

---

## Scripts

The `scripts/` directory contains automation tools for development and deployment:

### `install.sh`
**Purpose**: Environment setup and dependency installation

**Features:**
- Loads environment variables from `.env` file
- Installs Node.js dependencies for commitlint
- Sets up Husky git hooks automatically
- Validates environment configuration

**Usage:**
```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

### `build-image.sh`
**Purpose**: Advanced Docker image building with multiple options

**Features:**
- **Flexible tagging**: Custom image names and tags
- **Multi-platform builds**: Support for different architectures
- **Registry integration**: Automatic push to Docker registries
- **Build optimization**: Cache management and cleanup options
- **Environment validation**: Docker installation and daemon checks

**Usage Examples:**
```bash
# Basic build
./scripts/build-image.sh

# Build with custom tag
./scripts/build-image.sh -t v1.0.0

# Build and push to registry
./scripts/build-image.sh -t latest -p

# Clean build without cache
./scripts/build-image.sh --no-cache -c

# Multi-platform build
./scripts/build-image.sh --platform linux/amd64,linux/arm64

# Build with custom registry
DOCKER_REGISTRY=myregistry.com ./scripts/build-image.sh -t v1.0.0 -p
```

**Environment Variables for Registry Push:**
```bash
DOCKER_REGISTRY=your-registry.com
DOCKER_USERNAME=your-username  
DOCKER_PASSWORD=your-password-or-token
```

---

## Git Hooks

Git hooks are automatically installed via the `scripts/install.sh` script using **Husky**. They help enforce code quality and commit standards.

### Available Hooks

- **Commitlint**: Validates commit messages against conventional commit standards following [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
  - **Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
  - **Format**: `type(scope): description`
  - **Example**: `feat(auth): add OAuth2 login functionality`

- **Pre-commit**: Future integration for code formatting and linting
  - Java code formatting using Spotless (planned)
  - Import organization and style checks (planned)

**Manual Hook Setup:**
```bash
# If hooks aren't working, reinstall manually
npm install
npx husky install
```

## Directory Structure

```text
.
└── datnbe/
    ├── ai/
    ├── auth/
    ├── DatnBeApplication.java
    ├── document/
    └── sharedkernel/
```

**Key Features:**
- **Modular Architecture**: Clear separation using Spring Modulith
- **Database Support**: PostgreSQL with JSONB for complex data structures
- **AI Integration**: OpenAI and Google Vertex AI services
- **Container-ready**: Docker and Docker Compose support
- **Development Tools**: Git hooks, automated builds, health monitoring
- **Configuration Management**: Environment-based configuration with Docker profiles
