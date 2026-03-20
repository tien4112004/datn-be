# DATN Backend - Developer Onboarding Guide

Welcome to the DATN Backend project! This guide will help you get up and running quickly.

## Table of Contents

- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Development Workflow](#development-workflow)
- [Adding New Features](#adding-new-features)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

---

## Project Overview

This Backend is a **Modular Monolith** application built using **Spring Modulith** architecture. It provides:

- **AI-powered features**: Presentation generation, outline creation, image generation using OpenAI GPT and Google Vertex AI Gemini
- **Authentication & Authorization**: OAuth2/OIDC integration with Keycloak
- **Document Management**: Document storage and retrieval with PostgreSQL JSONB
- **Payment Integration**: Sepay and PayOS payment gateways
- **Student Management**: Class and student management features
- **Class Management**: CMS functionality

### Tech Stack

- **Java 21** with Spring Boot 3.5.4
- **Spring Modulith** for modular architecture
- **PostgreSQL** with JSONB support
- **Keycloak** for authentication
- **Docker & Docker Compose** for containerization
- **Gradle** for build automation
- **GitHub Actions** for CI/CD
- **Jenkins** for deployment automation

---

## Prerequisites

Before you begin, ensure you have the following installed:

### Required

- **Git**: [Download](https://git-scm.com/)
- **Java 21** (JDK): [Adoptium/Eclipse Temurin](https://adoptium.net/)
- **Docker Desktop**: [Download](https://www.docker.com/products/docker-desktop/)
  - Includes Docker and Docker Compose
- **Node.js** (v16+): For git hooks and commitlint - [Download](https://nodejs.org/)

### Optional (but recommended)

- **IntelliJ IDEA** or **VS Code** with Java extensions
- **Postman** or **Bruno** for API testing
- **PostgreSQL client** (pgAdmin, DBeaver) for database inspection

### Verify Installation

```bash
# Check Java version (should be 21)
java -version

# Check Docker
docker --version
docker compose version

# Check Node.js
node --version
npm --version

# Check Git
git --version
```

---

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/dndstudio-dev/AIELearning-BE.git
cd AIELearning-BE
```

### 2. Setup Environment Variables

```bash
# Copy the sample environment file
cp .env.sample .env
```

Edit `.env` and configure the required variables:

```bash
# Keycloak (Required for authentication)
KEYCLOAK_CLIENT_ID=ai-primary
KEYCLOAK_CLIENT_SECRET=your-secret-here
KEYCLOAK_ISSUER_URI=http://localhost:8082/realms/ai-primary-dev
KEYCLOAK_REALM_NAME=ai-primary-dev
KEYCLOAK_SERVER_URL=http://localhost:8082

# CORS (Frontend URLs)
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# S3-compatible storage (Rustfs/MinIO/Cloudflare R2/AWS S3)
S3_ENDPOINT=http://localhost:9000
S3_BUCKET=default
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_PUBLIC_URL=http://localhost:9000

# Firebase (for push notifications)
FIREBASE_CONFIG_PATH=file:./firebase-service-account.json

# AI API endpoints (if you have AI worker service)
AI_API_BASE_URL=http://localhost:8081
AI_API_OUTLINE_ENDPOINT=/api/outline/generate/stream
AI_API_PRESENTATION_ENDPOINT=/api/presentations/generate/stream
```

> **Note**: For development, you can use the Docker setup which handles most dependencies automatically.

### 3. Install Dependencies and Git Hooks

```bash
# Make the installer executable
chmod +x scripts/install.sh

# Run the installer
./scripts/install.sh
```

This script will:
- Install Node.js dependencies for commitlint
- Set up Husky git hooks for commit message validation
- Load environment variables

### 4. Run the Application

#### Option A: Using Docker (Recommended for beginners)

```bash
# Start databases (PostgreSQL, Keycloak)
docker-compose -f docker-compose.db.yml up -d

# Build and start the application
docker-compose up -d

# View logs
docker-compose logs -f datn-be
```

The application will be available at:
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Keycloak Admin**: http://localhost:8082 (admin/admin)
- **Health Check**: http://localhost:8080/actuator/health

#### Option B: Local Development (Recommended for active development)

```bash
# Start only databases using Docker
docker-compose -f docker-compose.db.yml up -d

# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Or run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 5. Verify Installation

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}
```

---

## Configuration Management

### Configuration Files

| File | When to Use | Purpose |
|------|-------------|---------|
| **`application.yml`** | Always (base config) | Default settings for all environments. Uses `${ENV_VAR:default}` pattern for flexibility. |
| **`application-docker.yml`** | Docker/production | Overrides for Docker containers (uses service names like `postgres`, `keycloak` instead of `localhost`). Loaded when `SPRING_PROFILES_ACTIVE=docker`. |
| **`.env`** | Local development | Sensitive values (secrets, API keys). **Never commit to git!** |
| **`.env.sample`** | Documentation | Template showing required environment variables. Safe to commit. |

### Key Configuration Concepts

**Environment Variable Pattern:**
```yaml
database-url: ${POSTGRES_DB_URL:jdbc:postgresql://localhost:5432/db}
```
- Uses `POSTGRES_DB_URL` from environment if set
- Falls back to default value after `:` if not set

**Configuration Loading Order** (later overrides earlier):
1. `application.yml` (base)
2. `.env` file
3. `application-docker.yml` (if profile=docker)
4. System environment variables
5. Command-line arguments

**Activate Docker Profile:**
```bash
# In docker-compose.yml
environment:
  - SPRING_PROFILES_ACTIVE=docker

# Or manually
./gradlew bootRun --args='--spring.profiles.active=docker'
```

### Adding New Configuration

**Quick example:**

1. Add to `application.yml`:
   ```yaml
   app:
     features:
       new-feature: ${NEW_FEATURE_ENABLED:false}
   ```

2. Create config class:
   ```java
   @Configuration
   @ConfigurationProperties(prefix = "app.features")
   @Data
   public class FeatureConfig {
       private boolean newFeature;
   }
   ```

3. Use it:
   ```java
   @Service
   @RequiredArgsConstructor
   public class MyService {
       private final FeatureConfig config;

       public void doSomething() {
           if (config.isNewFeature()) { ... }
       }
   }
   ```

4. Document in `.env.sample`:
   ```bash
   NEW_FEATURE_ENABLED=false
   ```

---

## Architecture

### Modular Monolith Structure

This project uses **Spring Modulith** to organize code into logical, loosely-coupled modules within a single deployable application.

```
src/main/java/com/datn/datnbe/
├── DatnBeApplication.java          # Main Spring Boot application
├── ai/                              # AI Module
│   ├── api/                         # Public API interfaces (for other modules)
│   ├── controller/                  # REST controllers
│   ├── presentation/                # REST controllers
│   ├── service/                     # Business logic services
│   ├── management/                  # Service implementations (implement api/)
│   ├── entity/                      # JPA entities
│   ├── repository/                  # JPA repositories
│   ├── dto/                         # Data transfer objects
│   ├── mapper/                      # MapStruct mappers
│   ├── config/                      # Module-specific configuration
│   ├── apiclient/                   # External API clients
│   └── utils/                       # Utility classes
├── auth/                            # Authentication Module
│   ├── api/                         # Public API (UserProfileApi, etc.)
│   ├── presentation/                # REST controllers
│   ├── service/                     # Auth business logic
│   ├── management/                  # API implementations
│   ├── entity/                      # User entities
│   ├── repository/                  # User repositories
│   ├── dto/                         # Request/Response DTOs
│   ├── mapper/                      # Object mappers
│   ├── config/                      # Security, JWT configuration
│   └── util/                        # Auth utilities
├── document/                        # Document Management Module
│   ├── api/                         # Public API (MediaStorageApi, PresentationApi, etc.)
│   ├── presentation/                # REST controllers
│   ├── service/                     # Document services
│   ├── management/                  # API implementations
│   ├── entity/                      # Document entities
│   ├── repository/                  # Document repositories
│   └── dto/                         # DTOs
├── payment/                         # Payment Module
│   ├── api/                         # Public API (PaymentApi)
│   ├── service/                     # Payment processing
│   └── ...                          # Similar structure
├── student/                         # Student Management Module
│   ├── api/                         # Public API (StudentApi, ClassApi)
│   └── ...                          # Similar structure
├── cms/                             # Content Management System Module
│   ├── api/                         # Public API (ClassApi, LessonApi, etc.)
│   └── ...                          # Similar structure
├── sharedkernel/                    # Shared Kernel (Common utilities)
│   ├── api/                         # Shared APIs
│   ├── dto/                         # Common DTOs
│   ├── exception/                   # Global exceptions
│   ├── security/                    # Security utilities
│   ├── service/                     # Shared services
│   └── utils/                       # Utility classes
└── config/                          # Global configuration classes
```

### Module Communication

Each module defines its public API in the `api/` package:
1. **API Interfaces** (`{module}/api/`): Define contracts for inter-module communication
2. **Implementations** (`{module}/management/` or `{module}/service/`): Implement these API interfaces
3. **Other modules** depend on these API interfaces

**Spring Modulith enforces boundaries** via `@ApplicationModule` annotation with `allowedDependencies`:

```java
// Example from ai/package-info.java
@ApplicationModule(allowedDependencies = {
    "document :: DocumentApi",        // Can use document module's API
    "sharedkernel",                   // Can use shared kernel
    "document :: DocumentRequestDto", // Can use document DTOs
    "sharedkernel::dto",              // Can use shared DTOs
    "sharedkernel::exceptions"        // Can use shared exceptions
})
package com.datn.datnbe.ai;
```

**Example: Document module calling Auth module**

```java
// In document module
@Service
public class PresentationService {
    private final UserProfileApi userProfileApi;  // Auth module's API interface

    public PresentationService(UserProfileApi userProfileApi) {
        this.userProfileApi = userProfileApi;
    }

    public void enrichPresentation(Presentation presentation) {
        // Direct method call via interface - no events!
        UserMinimalInfoDto userInfo = userProfileApi.getUserMinimalInfo(presentation.getOwnerId());
        // Use user info...
    }
}
```

**Module Boundaries:**
- Modules can only access other modules' `api/` packages
- Spring Modulith validates dependencies at compile-time
- Prevents circular dependencies and tight coupling

### Database Architecture

- **Single PostgreSQL Database**: All modules share one database (`datn_monolith_db`)
- **Tables per Module**: Each module manages its own tables
- **JSONB Support**: Complex data structures stored as JSONB columns
- **Hibernate JPA**: ORM with automatic DDL updates (development mode)

### Key Design Patterns

- **Modular Monolith**: Spring Modulith for bounded contexts and enforced boundaries
- **API-based Communication**: Modules interact via interfaces in `api/` packages
- **Repository Pattern**: JPA repositories for data access
- **DTO Pattern**: Request/Response DTOs separate from domain entities
- **Service Layer Pattern**: Business logic in `service/` and `management/` classes
- **Mapper Pattern**: MapStruct for entity ↔ DTO conversion

---

## Development Workflow

### Branch Strategy

- **`main`**: Production-ready code
- **`develop`**: Integration branch for features
- **Feature branches**: `feat/feature-name`, `fix/bug-name`

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description

Examples:
feat(auth): add OAuth2 login functionality
fix(payment): resolve transaction timeout issue
docs(readme): update setup instructions
refactor(document): simplify file upload logic
test(ai): add unit tests for outline generation
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Git hooks will automatically validate your commit messages.

### Code Quality

- **Spotless**: Java code formatting (Eclipse formatter)
- **Run before commit**:
  ```bash
  ./gradlew spotlessApply
  ```

### Development Cycle

1. **Pull latest changes**
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feat/your-feature-name
   ```

3. **Make changes and test**
   ```bash
   ./gradlew test
   ./gradlew bootRun
   ```

4. **Commit with conventional format**
   ```bash
   git add .
   git commit -m "feat(module): add new feature"
   ```

5. **Push and create PR**
   ```bash
   git push origin feat/your-feature-name
   ```
   Then create a Pull Request on GitHub targeting `develop`.

---

## Adding New Features

### Step-by-Step Guide

#### 1. Decide on Module Placement

Ask yourself:
- Does this feature belong to an existing module? (e.g., new auth method → `auth` module)
- Is it a new bounded context? Consider creating a new module.

**Existing Modules:**
- `ai` - AI features (model configuration, content generation, token usage)
- `auth` - Authentication, user profiles, permissions
- `document` - Documents, presentations, slides, media storage, questions
- `payment` - Payment processing (Sepay, PayOS)
- `student` - Student and class management
- `cms` - Content management (lessons, posts, assignments, submissions)
- `sharedkernel` - Shared utilities, DTOs, exceptions

#### 2. Create Entity (if needed)

**Location**: `src/main/java/com/datn/datnbe/{module}/entity/`

```java
package com.datn.datnbe.yourmodule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

@Entity
@Table(name = "your_entities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Use JSONB for complex objects
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private YourComplexType metadata;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### 3. Create Repository

**Location**: `src/main/java/com/datn/datnbe/{module}/repository/`

```java
package com.datn.datnbe.yourmodule.repository;

import com.datn.datnbe.yourmodule.entity.YourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface YourEntityRepository extends JpaRepository<YourEntity, Long> {

    // Custom query methods
    Optional<YourEntity> findByName(String name);

    List<YourEntity> findByCreatedAtAfter(LocalDateTime date);

    // Custom JPQL query
    @Query("SELECT e FROM YourEntity e WHERE e.name LIKE %:keyword%")
    List<YourEntity> searchByName(@Param("keyword") String keyword);
}
```

#### 4. Create DTOs

**Location**: `src/main/java/com/datn/datnbe/{module}/presentation/dto/`

```java
package com.datn.datnbe.yourmodule.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateYourEntityRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100)
    private String name;

    private YourComplexType metadata;
}
```java
// In Document Module - Publishing an event
publisher.publishEvent(new DocumentCreatedEvent(documentId, userId));

// In AI Module - Listening to the event
@EventListener
public void onDocumentCreated(DocumentCreatedEvent event) {
    // Process document for AI analysis
}
```
```java
// Response DTO
@Data
@Builder
public class YourEntityResponse {
    private Long id;
    private String name;
    private YourComplexType metadata;
    private LocalDateTime createdAt;
}
```

#### 5. Create Mapper (Optional but Recommended)

**Location**: `src/main/java/com/datn/datnbe/{module}/mapper/`

```java
package com.datn.datnbe.yourmodule.mapper;

import com.datn.datnbe.yourmodule.entity.YourEntity;
import com.datn.datnbe.yourmodule.dto.request.CreateYourEntityRequest;
import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface YourEntityMapper {

    YourEntity toEntity(CreateYourEntityRequest request);

    YourEntityResponse toResponse(YourEntity entity);

    List<YourEntityResponse> toResponseList(List<YourEntity> entities);
}
```

#### 6. Create Service

**Location**: `src/main/java/com/datn/datnbe/{module}/service/`

```java
package com.datn.datnbe.yourmodule.service;

import com.datn.datnbe.yourmodule.entity.YourEntity;
import com.datn.datnbe.yourmodule.repository.YourEntityRepository;
import com.datn.datnbe.yourmodule.dto.request.CreateYourEntityRequest;
import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import com.datn.datnbe.yourmodule.mapper.YourEntityMapper;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class YourEntityService {

    private final YourEntityRepository repository;
    private final YourEntityMapper mapper;

    @Transactional
    public YourEntityResponse create(CreateYourEntityRequest request) {
        log.info("Creating entity with name: {}", request.getName());

        // Map DTO to entity
        YourEntity entity = mapper.toEntity(request);

        // Save
        YourEntity saved = repository.save(entity);

        // Map back to response
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public YourEntityResponse getById(Long id) {
        YourEntity entity = repository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<YourEntityResponse> getAll() {
        List<YourEntity> entities = repository.findAll();
        return mapper.toResponseList(entities);
    }

    @Transactional
    public YourEntityResponse update(Long id, UpdateYourEntityRequest request) {
        YourEntity entity = repository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Update fields
        entity.setName(request.getName());
        entity.setMetadata(request.getMetadata());

        YourEntity updated = repository.save(entity);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        repository.deleteById(id);
    }
}
```

#### 7. Create Controller

**Location**: `src/main/java/com/datn/datnbe/{module}/controller/` or `src/main/java/com/datn/datnbe/{module}/presentation/`

```java
package com.datn.datnbe.yourmodule.controller;

import com.datn.datnbe.yourmodule.service.YourEntityService;
import com.datn.datnbe.yourmodule.dto.request.CreateYourEntityRequest;
import com.datn.datnbe.yourmodule.dto.request.UpdateYourEntityRequest;
import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/your-entities")
@RequiredArgsConstructor
@Tag(name = "Your Entity", description = "Your entity management APIs")
public class YourEntityController {

    private final YourEntityService service;

    @PostMapping
    @Operation(summary = "Create a new entity")
    public ResponseEntity<AppResponseDto<YourEntityResponse>> create(
            @Valid @RequestBody CreateYourEntityRequest request) {

        YourEntityResponse response = service.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AppResponseDto.success(response, "Entity created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all entities")
    public ResponseEntity<AppResponseDto<List<YourEntityResponse>>> getAll() {
        List<YourEntityResponse> responses = service.getAll();
        return ResponseEntity.ok(
            AppResponseDto.success(responses, "Entities retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID")
    public ResponseEntity<AppResponseDto<YourEntityResponse>> getById(
            @PathVariable Long id) {

        YourEntityResponse response = service.getById(id);
        return ResponseEntity.ok(
            AppResponseDto.success(response, "Entity retrieved successfully")
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update entity")
    public ResponseEntity<AppResponseDto<YourEntityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateYourEntityRequest request) {

        YourEntityResponse response = service.update(id, request);
        return ResponseEntity.ok(
            AppResponseDto.success(response, "Entity updated successfully")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity")
    public ResponseEntity<AppResponseDto<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(
            AppResponseDto.success(null, "Entity deleted successfully")
        );
    }
}

/**
 * Note: For document permission checking, use @RequireDocumentPermission annotation:
 *
 * @GetMapping("/{id}")
 * @RequireDocumentPermission(scopes = {"read"}, documentIdParam = "id")
 * public ResponseEntity<YourDocumentResponse> getDocument(@PathVariable String id) {
 *     // Permission automatically checked by aspect before execution
 *     return ...;
 * }
 */
```

#### 8. Create Module API (for Inter-Module Communication)

If other modules need to use your feature, create an API interface.

**Location**: `src/main/java/com/datn/datnbe/{module}/api/`

```java
package com.datn.datnbe.yourmodule.api;

import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import java.util.List;

/**
 * Public API for YourModule - other modules can depend on this
 */
public interface YourEntityApi {

    /**
     * Get entity by ID
     * @param id the entity ID
     * @return entity response or null if not found
     */
    YourEntityResponse getEntityById(Long id);

    /**
     * Get entities by IDs (batch operation)
     * @param ids list of entity IDs
     * @return list of entities
     */
    List<YourEntityResponse> getEntitiesByIds(List<Long> ids);
}
```

**Implementation** in `management/` or `service/`:

```java
package com.datn.datnbe.yourmodule.management;

import com.datn.datnbe.yourmodule.api.YourEntityApi;
import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import com.datn.datnbe.yourmodule.repository.YourEntityRepository;
import com.datn.datnbe.yourmodule.mapper.YourEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YourEntityManagement implements YourEntityApi {

    private final YourEntityRepository repository;
    private final YourEntityMapper mapper;

    @Override
    public YourEntityResponse getEntityById(Long id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElse(null);
    }

    @Override
    public List<YourEntityResponse> getEntitiesByIds(List<Long> ids) {
        return repository.findAllById(ids).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }
}
```

**Update module dependencies** in `package-info.java`:

```java
@ApplicationModule(allowedDependencies = {
    "sharedkernel",
    "auth :: authApi",  // Can use auth APIs
    "yourmodule :: YourModuleApi"  // Expose your API
})
package com.datn.datnbe.yourmodule;

import org.springframework.modulith.ApplicationModule;
```

**Create API package-info.java**:

```java
@NamedInterface("YourModuleApi")
package com.datn.datnbe.yourmodule.api;

import org.springframework.modulith.NamedInterface;
```

#### 9. Add Tests

**Location**: `src/test/java/com/datn/datnbe/{module}/`

**Unit Test Example:**

```java
package com.datn.datnbe.yourmodule.service;

import com.datn.datnbe.yourmodule.entity.YourEntity;
import com.datn.datnbe.yourmodule.repository.YourEntityRepository;
import com.datn.datnbe.yourmodule.mapper.YourEntityMapper;
import com.datn.datnbe.yourmodule.dto.request.CreateYourEntityRequest;
import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourEntityServiceTest {

    @Mock
    private YourEntityRepository repository;

    @Mock
    private YourEntityMapper mapper;

    @InjectMocks
    private YourEntityService service;

    @Test
    void create_WithValidData_ShouldReturnCreatedEntity() {
        // Given
        CreateYourEntityRequest request = new CreateYourEntityRequest();
        request.setName("Test Entity");

        YourEntity entity = YourEntity.builder()
            .name("Test Entity")
            .build();

        YourEntity savedEntity = YourEntity.builder()
            .id(1L)
            .name("Test Entity")
            .build();

        YourEntityResponse expectedResponse = new YourEntityResponse();
        expectedResponse.setId(1L);
        expectedResponse.setName("Test Entity");

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        // When
        YourEntityResponse response = service.create(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Entity", response.getName());
        verify(repository).save(entity);
    }
}
```

**Integration Test Example:**

```java
package com.datn.datnbe.yourmodule.controller;

import com.datn.datnbe.yourmodule.dto.request.CreateYourEntityRequest;
import com.datn.datnbe.yourmodule.dto.response.YourEntityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class YourEntityControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void create_WithValidData_ShouldReturn201() {
        // Given
        CreateYourEntityRequest request = new CreateYourEntityRequest();
        request.setName("Integration Test Entity");

        // When
        ResponseEntity<YourEntityResponse> response = restTemplate.postForEntity(
            "/api/your-entities",
            request,
            YourEntityResponse.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Integration Test Entity", response.getBody().getName());
    }
}
```

#### 10. Update API Documentation

Swagger/OpenAPI documentation is auto-generated from your `@Operation` and `@Tag` annotations.

Access it at: **http://localhost:8080/swagger-ui/index.html**

#### 11. Database Migration (Production)

For production, consider using Flyway or Liquibase for version-controlled migrations.

Currently using Hibernate auto-update (development only):
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Auto-creates tables (dev only)
```

---

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew test --tests "com.datn.datnbe.auth.*"

# Run specific test class
./gradlew test --tests "YourEntityServiceTest"

# Run with detailed output
./gradlew test --info

# Generate test coverage report
./gradlew test jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

### Test Structure

```
src/test/java/com/datn/datnbe/
├── ai/
│   ├── application/
│   │   └── ModelConfigurationServiceTest.java
│   └── presentation/
│       └── ModelConfigurationControllerTest.java
└── auth/
    └── ... (similar structure)
```

### Writing Tests

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test module interactions with real database (Testcontainers)
- **Controller Tests**: Use `@WebMvcTest` for REST endpoint testing

**Example Unit Test:**

```java
@ExtendWith(MockitoExtension.class)
class YourEntityServiceTest {

    @Mock
    private YourEntityRepository repository;

    @InjectMocks
    private YourEntityService service;

    @Test
    void getById_WithExistingId_ShouldReturnEntity() {
        // Given
        YourEntity entity = YourEntity.builder()
            .id(1L)
            .name("Test")
            .build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        YourEntityResponse response = service.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test", response.getName());
        verify(repository).findById(1L);
    }
}
```

---

## Deployment

### Deployment Architecture

```
GitHub → GitHub Actions (CI) → Docker Build → GHCR → Jenkins → Production Server
```

### CI/CD Pipeline

#### 1. Continuous Integration (GitHub Actions)

**Workflow**: `.github/workflows/ci.yml`

Triggered on:
- Push to `main` or `develop`
- Pull requests to `main` or `develop`

Steps:
1. Checkout code
2. Setup Java 21
3. Run tests
4. Upload test results

#### 2. Docker Build (GitHub Actions)

**Workflow**: `.github/workflows/docker-build.yml`

Triggered on:
- Successful CI workflow completion on `main`
- Manual workflow dispatch

Steps:
1. Build Docker image
2. Push to GitHub Container Registry (ghcr.io)
3. Tag with: `latest`, `sha-<commit>`, branch name
4. Trigger Jenkins deployment

#### 3. Deployment (Jenkins)

**Jenkinsfile**: `Jenkinsfile`

Steps:
1. Validate environment
2. Authenticate with GHCR
3. Pull latest image
4. Stop old containers
5. Deploy new containers using `docker-compose.prod.yml`
6. Run health checks
7. Cleanup old images

### Manual Deployment

#### Local Docker Build

```bash
# Build Docker image
./scripts/build-image.sh

# Build with custom tag
./scripts/build-image.sh -t v1.0.0

# Build and push to registry
./scripts/build-image.sh -t latest -p
```

#### Production Deployment

**On Production Server:**

1. **Pull latest image**
   ```bash
   docker login ghcr.io -u <username>
   docker pull ghcr.io/tien4112004/datn-be:latest
   ```

2. **Prepare environment**
   ```bash
   cd /opt/datn-be
   # Ensure .env.prod exists with production values
   ```

3. **Deploy with Docker Compose**
   ```bash
   # Start databases
   docker-compose -f docker-compose.db.prod.yml up -d

   # Start application
   docker-compose -f docker-compose.prod.yml up -d
   ```

4. **Verify deployment**
   ```bash
   # Check container status
   docker-compose -f docker-compose.prod.yml ps

   # Check logs
   docker-compose -f docker-compose.prod.yml logs -f datn-be

   # Health check
   curl http://localhost:8080/actuator/health
   ```

### Environment-Specific Configuration

The application uses Spring profiles:

- **`default`**: Local development
- **`docker`**: Docker container environment
- **`test`**: Testing environment

**Activate profile:**
```bash
# Via environment variable
export SPRING_PROFILES_ACTIVE=docker

# Via application property
--spring.profiles.active=docker

# In Docker Compose
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

### Monitoring & Observability

The application includes:

- **Actuator Endpoints**: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- **Prometheus Metrics**: Exposed at `/actuator/prometheus`
- **Loki Logging**: Logs shipped to Loki for centralized logging
- **Spring Modulith Observability**: Module interaction tracing

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

---

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Error**: `Address already in use`

**Solution**:
```bash
# Find process using port 8080
lsof -i :8080
# or
netstat -ano | grep 8080

# Kill the process
kill -9 <PID>
```

#### 2. Docker Container Won't Start

**Check logs**:
```bash
docker-compose logs datn-be
```

**Common causes**:
- Missing environment variables
- Database not ready (wait for healthy status)
- Port conflicts

**Solution**:
```bash
# Restart containers
docker-compose down
docker-compose -f docker-compose.db.yml up -d
# Wait 30 seconds for DB initialization
docker-compose up -d
```

#### 3. Database Connection Failed

**Error**: `Connection refused` or `Unknown database`

**Check database status**:
```bash
docker-compose -f docker-compose.db.yml ps
```

**Verify connection**:
```bash
docker exec -it postgres-monolith psql -U postgres -d datn_monolith_db
```

#### 4. Gradle Build Failed

**Error**: `Compilation failed`

**Solution**:
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# If Gradle wrapper issues
./gradlew wrapper --gradle-version=8.5
```

#### 5. Tests Failing

**Check test output**:
```bash
./gradlew test --info --stacktrace
```

**Common causes**:
- Missing test database
- Incorrect test data
- Environment variable issues

**Solution**:
```bash
# Use H2 in-memory DB for tests (already configured)
# Check application-test.yml configuration
```

#### 6. Authentication Issues

**Error**: `401 Unauthorized` or `403 Forbidden`

**Verify Keycloak**:
```bash
# Check Keycloak is running
docker-compose -f docker-compose.yml ps keycloak

# Access Keycloak admin: http://localhost:8082
# Username: admin, Password: admin
```

**Check realm configuration**:
- Realm: `ai-primary-dev`
- Client ID: `ai-primary`
- Ensure client secret matches `.env`

#### 7. Out of Memory Errors

**Solution**:
```bash
# Increase Gradle memory
export GRADLE_OPTS="-Xmx4096m"

# Or edit gradle.properties
org.gradle.jvmargs=-Xmx4096m
```

### Getting Help

- **Documentation**: Check `/docs` folder and inline code comments
- **API Docs**: http://localhost:8080/swagger-ui/index.html
- **Logs**: `docker-compose logs -f` or `./logs/` directory
- **Team Chat**: Contact your team lead or senior developer

---

## Additional Resources

### Documentation

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Modulith Reference](https://docs.spring.io/spring-modulith/reference/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Docker Documentation](https://docs.docker.com/)
- [Conventional Commits](https://www.conventionalcommits.org/)

### Project Files

- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Source Code**: `src/main/java/com/datn/datnbe/`
- **Configuration**: `src/main/resources/application.yml`
- **Docker Compose**: `docker-compose.yml`, `docker-compose.db.yml`
- **Build Config**: `build.gradle`
- **CI/CD**: `.github/workflows/`, `Jenkinsfile`

### Next Steps

1. ✅ Setup local environment
2. ✅ Run the application
3. ✅ Explore the codebase
4. 📖 Read module-specific README files (if available)
5. 🧪 Run and write tests
6. 🚀 Create your first feature
7. 📝 Submit your first PR

---

**Welcome to the team! Happy coding! 🚀**

If you have questions or run into issues, don't hesitate to ask your teammates or check the troubleshooting section.
