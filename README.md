## DATN Backend

A Spring Boot modular monolith backend for the DATN project, built with Spring Modulith, featuring authentication, document management, AI services, and API gateway functionality in a single deployable application.

<!-- Add table of contents -->

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
- [Database Configuration](#database-configuration)
  - [1. Fully Docker Setup (Recommended)](#1-fully-docker-setup-recommended)
  - [2. Local Database Setup](#2-local-database-setup)
- [Modules](#modules)
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

---

## Getting Started

Follow these steps to get the backend up and running.

### 1. Clone the Repository

```bash
git clone https://github.com/Tondeptrai23/datn-be.git
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
> - The installer script (`script/install.sh`) automatically loads `.env`.
> - If you skip the installer, load variables manually:
    >
    >   ```bash
>   source .env
>   ```

### 3. Install Git Hooks & Dependencies

Make the installer executable and run it:

```bash
chmod +x script/install.sh
./script/install.sh
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

1. **Build and run the application using Docker**

```bash
# Build the Docker image
docker build -t datn-be:latest .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e DATABASE_URL=your_db_url \
  -e DATABASE_USERNAME=your_username \
  -e DATABASE_PASSWORD=your_password \
  datn-be:latest
```

2. **Using Docker Compose**

```bash
# Start with databases
docker-compose -f docker-compose.db.yml up -d

# Start the application
docker-compose up -d
```

---

## Database Configuration

The application supports two database setup modes:

### 1. Fully Docker Setup (Recommended)

Uses containerized databases - **no database environment variables needed** in `.env`.

- PostgreSQL containers for auth and model configuration databases
- MongoDB container for document storage
- All connection details are handled automatically via Docker networking

To start with databases:

```bash
docker-compose -f docker-compose.db.yml -f docker-compose.yml up -d
```

### 2. Local Database Setup

For connecting to local or external databases, configure these variables in `.env`:

**PostgreSQL (Auth & Model Config):**

```bash
AUTH_DB_URL=jdbc:postgresql://localhost:5432/auth_db
AUTH_DB_USERNAME=postgres
AUTH_DB_PASSWORD=postgres

MODEL_CONFIG_DB_URL=jdbc:postgresql://localhost:5433/model_configuration_db
MODEL_CONFIG_DB_USERNAME=postgres
MODEL_CONFIG_DB_PASSWORD=postgres
```

**MongoDB (Document Service):**

Option A - Using connection URI:

```bash
MONGODB_URI=mongodb://localhost:27017/presentation_db?authSource=admin
```

Option B - Using individual variables:

```bash
MONGODB_USERNAME=mongouser
MONGODB_PASSWORD=mongopassword
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DATABASE=presentation_db
MONGODB_AUTH_DATABASE=admin
```

After updating `.env`, reload environment variables:

```bash
source .env
# or run the installer again
./script/install.sh
```

---

## Modules

This application is built as a modular monolith using Spring Modulith. The main modules are:

- **Auth Module** (`com.datn.datnbe.auth`): Handles user authentication and authorization
- **Document Module** (`com.datn.datnbe.document`): Manages document storage and retrieval using MongoDB
- **AI Module** (`com.datn.datnbe.ai`): Provides AI-powered features using OpenAI and Vertex AI
- **Gateway Module** (`com.datn.datnbe.sharedkernel`): Internal routing and request handling

Each module is self-contained with its own:
- Domain models
- Service layer
- Repository/Data access layer
- REST controllers
- Configuration

Spring Modulith ensures proper module boundaries and dependencies are respected at compile time.

---

## Git Hooks

Git hooks are automatically installed via the `script/install.sh` script. They help enforce code quality and commit
standards.

### Available Hooks

- **Commitlint**: Validates commit messages against conventional commit standards. It
  follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
- **Java Format**: Ensures Java code is formatted according to project standards using Spotless.

## Directory Structure

```text
datn-be/
├── api-gateway/             # Spring Cloud Gateway
├── auth-service/            # Authentication microservice
├── presentation-service/    # Presentation microservice
├── config-server/           # Spring Cloud Config server
├── discovery-service/       # Eureka Discovery server
├── admin-server/            # Spring Boot Admin server
├── docker-compose.db.yml    # Database definitions
├── docker-compose.infra.yml # Infrastructure services
├── docker-compose.business.yml # Business services
├── docker-compose.yml        # Combined Docker Compose file
├── script/                  # Build & install scripts
├── .env.sample              # Sample environment variables
└── pom.xml                  # Parent Maven POM
```
