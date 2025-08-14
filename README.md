## DATN Backend

A Spring Boot microservices backend for the DATN project, with support for authentication, presentation, and API gateway
services.

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
        - [a. Without Docker Compose](#a-without-docker-compose)
        - [b. With Docker Compose](#b-with-docker-compose)
- [Databases](#databases)
- [Git Hooks](#git-hooks)
    - [Available Hooks](#available-hooks)
- [Directory Structure](#directory-structure)

---

### Prerequisites

Ensure the following are installed on your development machine:

- **Git**: [https://git-scm.com/](https://git-scm.com/)
- **Docker & Docker Compose**: [https://docs.docker.com/](https://docs.docker.com/)
- **Java 21** (or later): [https://adoptium.net/](https://adoptium.net/)
- **Maven**: [https://maven.apache.org/](https://maven.apache.org/)
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

#### a. Without Docker Compose

1. Ensure your local database URLs are correctly set in `.env` (see [Database Configuration](#database-configuration)
   section).

2. Create `config-server/src/main/resources/application-dev.yml` with the following content:

```yaml
spring:
  cloud:
    config:
      server:
        git:
          username: ${GIT_USERNAME:your_username}
          password: ${GIT_TOKEN:your_token}
```

3. Start the Spring Boot apps via Maven:

```bash
mvn spring-boot:run -pl <module-name>
```

#### b. With Docker Compose

Use the provided scripts to build images and start services :

1. **Build Docker Images**

- All images:

```bash
chmod +x script/build-images.sh
./script/build-images.sh
```

- Only business services (auth, presentation, api-gateway):

```bash
./script/build-images.sh --business-only
```

2. **Start Services**

- **Databases** (PostgreSQL & MongoDB):

```bash
docker-compose -f docker-compose.db.yml up -d
```

- **Infrastructure** (Config Server, Discovery Service, Admin Server):

```bash
docker-compose -f docker-compose.infra.yml up -d
```

- **Business Services** (Auth, Presentation, API Gateway):

```bash
docker-compose -f docker-compose.business.yml up -d
```

- **Run all services together**:

```bash
docker-compose up -d
```

**Fully Docker Setup Options:**

- **Option 1**: Run all services including databases:

```bash
docker-compose -f docker-compose.db.yml -f docker-compose.yml up -d
```

- **Option 2**: Start databases first, then other services:

```bash
docker-compose -f docker-compose.db.yml up -d
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

## Git Hooks

Git hooks are automatically installed via the `script/install.sh` script. They help enforce code quality and commit
standards.

### Available Hooks

- **Commitlint**: Validates commit messages against conventional commit standards. It
  follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
- **Java Format**: Ensures Java code is formatted according to project standards.

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
