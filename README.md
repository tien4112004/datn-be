## DATN Backend

A Spring Boot modular monolith backend for the DATN project, built with Spring Modulith, featuring authentication, document management, AI services, and presentation functionality in a single deployable application.

## 📚 Documentation

- **[Developer Onboarding Guide](NEW_DEVELOPER_GUIDE.md)** - Complete guide for new developers (setup, architecture, adding features)
- **[Keycloak Setup Guide](KEYCLOAK_SETUP_GUIDE.md)** - How to configure Keycloak (authentication, authorization, Google login)
- **[Keycloak Backend Integration](KEYCLOAK_BACKEND_INTEGRATION.md)** - How the backend integrates with Keycloak (code examples, API flows)

## Table of Contents

- [Quick Start](#quick-start)
- [Tech Stack](#tech-stack)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Available Scripts](#available-scripts)
- [Git Hooks](#git-hooks)

## Quick Start

> For detailed setup instructions, architecture explanation, and feature development guide, see [Developer Onboarding Guide](NEW_DEVELOPER_GUIDE.md).

**Prerequisites:** Git, Docker & Docker Compose, Java 21, Gradle, Node.js

**Setup:**
```bash
# Clone and configure
git clone https://github.com/tien4112004/datn-be.git
cd datn-be
cp .env.sample .env

# Install dependencies and hooks
chmod +x scripts/install.sh
./scripts/install.sh

# Run with Docker (recommended)
docker-compose up -d

# Or run locally
./gradlew bootRun
```

## Tech Stack

- **Java 21** with **Spring Boot 3.5.4**
- **Spring Modulith** for modular monolith architecture
- **PostgreSQL** with JSONB support
- **Keycloak** for authentication and authorization (OAuth2/OIDC, UMA 2.0)
- **OpenAI GPT** and **Google Vertex AI Gemini** for AI services
- **Docker** for containerization

---

## Testing

Tests are organized by module using **JUnit 5**, **Mockito**, and **Spring Boot Test**.

**Common Commands:**
```bash
# Run all tests
./gradlew test

# Run tests by module
./gradlew test --tests "com.datn.datnbe.ai.*"
./gradlew test --tests "com.datn.datnbe.auth.*"

# Run specific test class
./gradlew test --tests "ModelConfigurationControllerTest"

# Run with coverage report
./gradlew test jacocoTestReport

# Run in parallel
./gradlew test --parallel
```

---

## Project Structure

Built as a **modular monolith** using **Spring Modulith**:

- **Auth Module** (`com.datn.datnbe.auth`): Authentication and authorization
- **Document Module** (`com.datn.datnbe.document`): Document storage and presentation generation
- **AI Module** (`com.datn.datnbe.ai`): AI-powered features (OpenAI GPT, Vertex AI Gemini)
- **Shared Kernel** (`com.datn.datnbe.sharedkernel`): Common utilities and cross-module components

> For architecture details and module structure, see [Developer Onboarding Guide](NEW_DEVELOPER_GUIDE.md#architecture).

---

## Available Scripts

**`install.sh`** - Environment setup and dependency installation
```bash
./scripts/install.sh
```

**`build-image.sh`** - Docker image building with custom tags and registry push
```bash
./scripts/build-image.sh              # Basic build
./scripts/build-image.sh -t v1.0.0    # With custom tag
./scripts/build-image.sh -t latest -p # Build and push to registry
```

---

## Git Hooks

Git hooks are automatically installed via `scripts/install.sh` using **Husky**.

**Commitlint** - Validates commit messages following [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- Format: `type(scope): description`
- Example: `feat(auth): add OAuth2 login functionality`
