## DATN Backend

A Spring Boot microservices backend for the DATN project, with support for authentication, presentation, and API gateway services.

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

2. Open `.env` and update any values as needed.

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

Ensure your local database URLs are correctly set in `.env` (see [Databases](#databases) section), then start the Spring Boot apps via Maven:

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

Note: This doesn't include databases; run `docker-compose -f docker-compose.db.yml up -d` first.

---

## Databases

By default, Docker Compose creates PostgreSQL and MongoDB containers. To use local databases instead:

1. Update your `.env` with local connection strings:

```bash
AUTH_DB_URL=jdbc:postgresql://localhost:5432/auth_db
AUTH_DB_USERNAME=postgres
AUTH_DB_PASSWORD=postgres
MONGODB_URI=mongodb://localhost:27017/presentation_db
```

2. Reload environment variables:

```bash
source .env
# or
./script/install.sh
```

The application will now connect to your local databases.

---

## Git Hooks

Git hooks are automatically installed via the `script/install.sh` script. They help enforce code quality and commit standards.

### Available Hooks

- **Commitlint**: Validates commit messages against conventional commit standards. It follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
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
