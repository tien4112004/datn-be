# Keycloak Setup Guide for DATN Backend

This guide will help you set up Keycloak to provide authentication and authorization for the DATN Backend application.

## Table of Contents

- [Introduction](#introduction)
- [Quick Setup (Using Realm Import)](#quick-setup-using-realm-import)
- [Manual Setup (Step-by-Step)](#manual-setup-step-by-step)
- [Understanding the Configuration](#understanding-the-configuration)
- [Managing Users and Roles](#managing-users-and-roles)
- [Testing the Setup](#testing-the-setup)
- [Advanced Configuration](#advanced-configuration)
- [Troubleshooting](#troubleshooting)

---

## Introduction

### What is Keycloak?

Keycloak is an open-source Identity and Access Management (IAM) solution that provides:
- **Single Sign-On (SSO)**
- **OAuth 2.0 / OpenID Connect (OIDC)** authentication
- **User management** and role-based access control (RBAC)
- **Social login** integration (Google, Facebook, etc.)

### How We Use Keycloak

The DATN Backend uses Keycloak for:
- **User Authentication**: OAuth2 authorization code flow
- **JWT Token Generation**: Access tokens with user roles
- **Role-Based Access Control**: `admin` and `user` roles
- **API Authorization**: Protecting endpoints based on roles

### Architecture Overview

```
User → Frontend → Backend (Spring Security) → Keycloak
                    ↓
              JWT Token Validation
              Role Extraction
              Access Control
```

**Token Flow:**
1. User logs in via frontend
2. Frontend redirects to Keycloak
3. Keycloak authenticates user
4. Keycloak issues JWT access token
5. Frontend sends JWT in requests (cookie or Authorization header)
6. Backend validates JWT and extracts roles
7. Backend enforces authorization rules

---

## Quick Setup (Using Realm Import)

This is the **fastest way** to get Keycloak running with pre-configured settings.

### Prerequisites

- Docker and Docker Compose installed
- Repository cloned

### Steps

#### 1. Start Keycloak with Docker Compose

```bash
# From the project root
docker-compose -f docker-compose.yml up -d keycloak

# Or start the full stack including database
docker-compose -f docker-compose.db.yml -f docker-compose.yml up -d
```

This automatically:
- Starts Keycloak on port **8082**
- Imports the realm configuration from `keycloak-data/ai-primary-dev-realm.json`
- Sets up the admin user (username: `admin`, password: `admin`)

#### 2. Access Keycloak Admin Console

Open your browser and navigate to:
```
http://localhost:8082
```

**Login credentials:**
- **Username**: `admin`
- **Password**: `admin`

#### 3. Verify Realm Configuration

1. Click on the **realm dropdown** (top-left) and select **`ai-primary-dev`**
2. You should see the pre-configured realm with:
   - Client: `ai-primary`
   - Roles: `admin`, `user`
   - Service account user

#### 4. Get Client Secret

1. Go to **Clients** → **ai-primary**
2. Go to **Credentials** tab
3. Copy the **Client Secret**
4. Update your `.env` file:

```bash
KEYCLOAK_CLIENT_SECRET=<paste-your-secret-here>
```

#### 5. Create Test Users

See [Managing Users and Roles](#managing-users-and-roles) section below.

#### 6. Start the Backend

```bash
# Load environment variables
source .env

# Start the application
./gradlew bootRun
```

**Done!** Your Keycloak is configured and ready to use.

---

## Manual Setup (Step-by-Step)

Use this if you want to configure Keycloak from scratch or understand the configuration in detail.

### Step 1: Start Keycloak

```bash
docker run -d \
  --name keycloak \
  -p 8082:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0.5 \
  start-dev
```

Wait ~30 seconds for Keycloak to start, then access: http://localhost:8082

### Step 2: Create a New Realm

1. **Login** to Keycloak Admin Console (admin/admin)
2. Hover over **master** realm dropdown (top-left)
3. Click **Create Realm**
4. Enter realm name: **`ai-primary-dev`**
5. Click **Create**

### Step 3: Configure Realm Settings

1. Go to **Realm Settings** (left sidebar)
2. Under **General** tab:
   - **User registration**: Enabled (if you want self-registration)
   - **Email as username**: Disabled
   - **Login with email**: Disabled
   - **Verify email**: Disabled (for development)
   - **Forgot password**: Enabled
   - **Remember me**: Enabled

3. Under **Login** tab:
   - **User registration**: ON (optional)
   - **Forgot password**: ON
   - **Remember Me**: ON

4. Under **Tokens** tab (important!):
   - **Access Token Lifespan**: 5 minutes (300s)
   - **SSO Session Idle**: 30 minutes
   - **SSO Session Max**: 10 hours
   - **Access Token Lifespan For Implicit Flow**: 15 minutes

5. Click **Save**

### Step 4: Create Client (ai-primary)

1. Go to **Clients** (left sidebar)
2. Click **Create client**
3. **General Settings**:
   - **Client type**: OpenID Connect
   - **Client ID**: `ai-primary`
   - Click **Next**

4. **Capability config**:
   - **Client authentication**: ON (confidential client)
   - **Authorization**: ON
   - **Authentication flow**:
     - ✅ Standard flow
     - ✅ Direct access grants
     - ✅ Service accounts roles
   - Click **Next**

5. **Login settings**:
   - **Root URL**: `http://localhost:8080`
   - **Home URL**: `http://localhost:8080`
   - **Valid redirect URIs**:
     ```
     http://localhost:8080/login/oauth2/code/*
     http://localhost:5173/*
     http://localhost:3000/*
     ```
   - **Valid post logout redirect URIs**: `+`
   - **Web origins**: `*` (for development; restrict in production!)
   - Click **Save**

6. Go to **Credentials** tab:
   - Copy the **Client Secret**
   - Save it to your `.env` file:
     ```bash
     KEYCLOAK_CLIENT_SECRET=<your-client-secret>
     ```

7. Go to **Advanced** tab:
   - **Advanced Settings** → **Access Token Lifespan**: Leave default or set to 5 minutes

### Step 5: Create Realm Roles

1. Go to **Realm roles** (left sidebar)
2. Click **Create role**
3. Create the following roles:

   **Role 1: admin**
   - **Role name**: `admin`
   - **Description**: `Administrator role with full access`
   - Click **Save**

   **Role 2: user**
   - **Role name**: `user`
   - **Description**: `Standard user role`
   - Click **Save**

### Step 6: Create Users

#### Create Admin User

1. Go to **Users** (left sidebar)
2. Click **Create new user**
3. **User details**:
   - **Username**: `admin`
   - **Email**: `admin@example.com` (optional)
   - **Email verified**: ON
   - **Enabled**: ON
   - Click **Create**

4. **Set Password**:
   - Go to **Credentials** tab
   - Click **Set password**
   - **Password**: `admin123` (or your choice)
   - **Temporary**: OFF (disable if you don't want to force password change)
   - Click **Save**
   - Confirm password reset

5. **Assign Roles**:
   - Go to **Role mapping** tab
   - Click **Assign role**
   - Filter by **Realm roles**
   - Select `admin` and `user` roles
   - Click **Assign**

#### Create Regular User

Repeat the process above with:
- **Username**: `testuser`
- **Email**: `testuser@example.com`
- **Password**: `password123`
- **Roles**: `user` (only)

### Step 7: Configure Client Scopes (Optional but Recommended)

This ensures roles are properly included in the JWT token.

1. Go to **Client scopes** (left sidebar)
2. Click on **roles** scope
3. Go to **Mappers** tab
4. Click on **realm roles** mapper
5. Ensure the following settings:
   - **Token Claim Name**: `realm_access.roles`
   - **Add to ID token**: ON
   - **Add to access token**: ON
   - **Add to userinfo**: ON
6. Click **Save**

### Step 8: Configure Backend Application

Update your `.env` file with Keycloak configuration:

```bash
# Keycloak Configuration
KEYCLOAK_CLIENT_ID=ai-primary
KEYCLOAK_CLIENT_SECRET=<your-client-secret-from-step-4>
KEYCLOAK_REDIRECT_URI=http://localhost:8080/login/oauth2/code/keycloak
KEYCLOAK_ISSUER_URI=http://localhost:8082/realms/ai-primary-dev
KEYCLOAK_REALM_NAME=ai-primary-dev
KEYCLOAK_SERVER_URL=http://localhost:8082

# Keycloak Admin (for programmatic user management)
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
```

### Step 9: Start the Backend

```bash
# Load environment variables
source .env

# Start the application
./gradlew bootRun
```

---

## Understanding the Configuration

### Realm: ai-primary-dev

A **realm** is an isolated security domain in Keycloak. All users, roles, and clients within a realm are separate from other realms.

**Our realm settings:**
- **Name**: `ai-primary-dev`
- **Registration allowed**: Yes (users can self-register)
- **Email verification**: Disabled (for development)
- **SSL required**: External (only HTTPS in production)

### Client: ai-primary

A **client** represents an application that wants to use Keycloak for authentication.

**Configuration:**
- **Client ID**: `ai-primary`
- **Client Type**: Confidential (requires client secret)
- **Authentication Flow**:
  - Authorization Code Flow (standard OAuth2 flow)
  - Direct Access Grants (username/password login)
  - Service Accounts (for backend-to-backend communication)
- **Valid Redirect URIs**: Where Keycloak can redirect after login
- **Web Origins**: CORS allowed origins

### Roles

**Realm Roles** are global roles within the realm.

| Role    | Description                      | Access Level                          |
|---------|----------------------------------|---------------------------------------|
| `admin` | Administrator                    | Full access to all endpoints          |
| `user`  | Standard user                    | Access to user-specific endpoints     |

**How roles are used in the backend:**

```java
// From SecurityConfig.java

// Admin endpoints - requires ADMIN role
.requestMatchers("/api/admin/**")
    .hasRole("admin")

// API endpoints - requires USER or ADMIN role
.requestMatchers("/api/**")
    .hasAnyRole("user", "admin")

// Authenticated endpoints - any authenticated user
.requestMatchers("/api/resources/**", "/api/classes/**")
    .authenticated()
```

### JWT Token Structure

When a user logs in, Keycloak issues a JWT token that looks like this:

```json
{
  "exp": 1710432000,
  "iat": 1710431700,
  "jti": "unique-token-id",
  "iss": "http://localhost:8082/realms/ai-primary-dev",
  "sub": "user-uuid",
  "typ": "Bearer",
  "azp": "ai-primary",
  "realm_access": {
    "roles": [
      "user",
      "admin"
    ]
  },
  "scope": "openid profile email",
  "email_verified": false,
  "name": "Admin User",
  "preferred_username": "admin",
  "email": "admin@example.com"
}
```

**Key claims:**
- **`sub`**: User ID (UUID)
- **`preferred_username`**: Username
- **`realm_access.roles`**: User's roles
- **`exp`**: Token expiration time
- **`iss`**: Token issuer (Keycloak realm URL)

**Role Extraction:**

The `JwtConverter` class extracts roles from the JWT and converts them to Spring Security authorities:

```java
// From JwtConverter.java
// Extracts "admin" from realm_access.roles
// Converts to "ROLE_admin" (Spring Security format)
```

---

## Managing Users and Roles

### Creating Users via Admin Console

1. Go to **Users** → **Create new user**
2. Fill in user details
3. Set password in **Credentials** tab
4. Assign roles in **Role mapping** tab

### Common User Operations

#### Assign Role to User

1. Go to **Users** → Select user
2. **Role mapping** tab
3. Click **Assign role**
4. Select role(s) and click **Assign**

#### Change User Password

1. Go to **Users** → Select user
2. **Credentials** tab
3. Click **Set password**
4. Enter new password
5. **Temporary**: OFF (to prevent forced password change)
6. Click **Save**

#### Disable/Enable User

1. Go to **Users** → Select user
2. Toggle **Enabled** switch
3. Click **Save**

#### Delete User

1. Go to **Users** → Select user
2. Click **Delete**
3. Confirm deletion

### Creating Users Programmatically

The backend can create users using Keycloak Admin Client (see `UserService` if implemented).

Example use case:
- Self-registration via API
- Bulk user import
- Integration with external systems

---

## Google Login Setup

### Overview

The application supports **Google OAuth login** via Keycloak's Identity Provider feature. Users can sign in with their Google account, and Keycloak automatically creates a user account.

### Prerequisites

1. Google Cloud Console account
2. OAuth 2.0 credentials (Client ID and Secret)

### Step 1: Create Google OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Google+ API** (if not already enabled)
4. Go to **Credentials** → **Create Credentials** → **OAuth client ID**
5. **Application type**: Web application
6. **Authorized JavaScript origins**:
   ```
   http://localhost:8082
   https://your-keycloak-domain.com
   ```
7. **Authorized redirect URIs**:
   ```
   http://localhost:8082/realms/ai-primary-dev/broker/google/endpoint
   https://your-keycloak-domain.com/realms/ai-primary-dev/broker/google/endpoint
   ```
8. Click **Create**
9. Copy **Client ID** and **Client Secret**

### Step 2: Configure Google Identity Provider in Keycloak

1. Login to Keycloak Admin Console
2. Select **ai-primary-dev** realm
3. Go to **Identity Providers** (left sidebar)
4. Click **Add provider** → Select **Google**
5. Fill in configuration:
   - **Alias**: `google` (important!)
   - **Display Name**: `Google`
   - **Enabled**: ON
   - **Client ID**: Paste from Google Console
   - **Client Secret**: Paste from Google Console
   - **Default Scopes**: `openid profile email`
   - **Trust Email**: ON
   - **First Login Flow**: `first broker login`
   - **Sync Mode**: `IMPORT`
6. Click **Save**

### Step 3: Configure First Broker Login Flow (Optional)

This determines what happens when a user logs in via Google for the first time.

1. Go to **Authentication** (left sidebar)
2. Select **first broker login** flow
3. The default flow:
   - **Review Profile**: User can review/edit their profile
   - **Create User If Unique**: Automatically create user if email is unique

Keep defaults unless you have specific requirements.

### Step 4: Add Google Button to Login Page

The Google login button appears automatically on the Keycloak login page once the identity provider is configured.

**Login Flow:**
1. User clicks "Sign in with Google"
2. Redirected to Google OAuth consent screen
3. After Google authentication, redirected back to Keycloak
4. Keycloak creates user account (first time) or logs in existing user
5. Application receives Keycloak access token

### Step 5: Test Google Login

1. Navigate to: `http://localhost:8080/oauth2/authorization/keycloak`
2. Click **Google** button on Keycloak login page
3. Authenticate with Google
4. Should be redirected back to application with access token

### Backend Integration

The application handles Google OAuth callback in `AuthController`:

```java
@GetMapping("/google/callback")
public void handleGoogleCallback(
    @RequestParam String code,
    @RequestParam String state,
    HttpServletResponse response
) {
    // Exchange authorization code for tokens
    SignInResponse tokens = oAuthCallbackService.processCallback(request);

    // Redirect to frontend with tokens
    response.sendRedirect(feUrl + "/auth/google/callback");
}
```

**Environment Variables:**
```bash
GOOGLE_CALLBACK_URI=http://localhost:8080/api/auth/google/callback
FE_REDIRECT_URI=http://localhost:5173/auth/google/callback
```

---

## Resource-Based Authorization Setup

### Overview

The application uses **Keycloak Authorization Services** for fine-grained, resource-based access control. This allows per-document permissions (read, comment, edit).

**Key Concepts:**
- **Resources**: Documents, presentations, assignments (e.g., `resource-presentation-123`)
- **Scopes**: Actions on resources (`read`, `comment`, `edit`)
- **Policies**: Rules defining who can access (user-based, group-based)
- **Permissions**: Combine resources + scopes + policies

### Prerequisites

Authorization Services must be enabled on the client (already done in realm export).

### Step 1: Verify Authorization Services are Enabled

1. Go to **Clients** → **ai-primary**
2. **Settings** tab:
   - **Authorization Enabled**: ON
3. If not enabled, enable it and click **Save**

### Step 2: Configure Authorization Scopes

1. Go to **Clients** → **ai-primary** → **Authorization** tab
2. Click **Scopes** (sub-tab)
3. Create scopes if not exist:

   **Scope: read**
   - **Name**: `read`
   - **Display Name**: `Read Access`
   - **Icon URI**: (optional)
   - Click **Save**

   **Scope: comment**
   - **Name**: `comment`
   - **Display Name**: `Comment Access`
   - Click **Save**

   **Scope: edit**
   - **Name**: `edit`
   - **Display Name**: `Edit Access`
   - Click **Save**

### Step 3: Understanding Resource Naming Convention

Resources are created programmatically by the backend:

**Format**: `resource-{resourceType}-{resourceId}`

**Examples:**
- `resource-presentation-456` (presentation with ID 456)
- `resource-assignment-789` (assignment with ID 789)
- `resource-mindmap-123` (mindmap with ID 123)

### Step 4: How Policies Work

The backend creates policies automatically when sharing resources:

**Owner Policy** (created when document is created):
```
Name: resource-presentation-456-owner-policy
Users: [owner-user-id]
Logic: POSITIVE
```

**User Policy** (created when sharing with specific user):
```
Name: resource-presentation-456-user-abc123-policy
Users: [abc123]
Logic: POSITIVE
```

**Group Policy** (created when sharing with a class):
```
Name: resource-presentation-456-group-class-xyz-policy
Groups: [/class-xyz]
Logic: POSITIVE
```

### Step 5: How Permissions Work

Permissions link resources, scopes, and policies:

**Owner Permission** (full access):
```
Name: resource-presentation-456-permission
Resource: resource-presentation-456
Scopes: read, comment, edit
Policies: resource-presentation-456-owner-policy
Decision Strategy: UNANIMOUS
```

**Read Permission** (shared with read access):
```
Name: resource-presentation-456-user-abc123-read-permission
Resource: resource-presentation-456
Scopes: read
Policies: resource-presentation-456-user-abc123-policy
Decision Strategy: UNANIMOUS
```

### Step 6: Backend Resource Registration

When a document is created, the backend automatically:

1. **Creates Keycloak Resource**:
   ```java
   KeycloakResourceDto resource = keycloakAuthzService.createResource(
       "resource-presentation-456",
       "Presentation: My Slides",
       "/api/presentations/456",
       ownerId
   );
   ```

2. **Creates Owner Policy**:
   ```java
   keycloakAuthzService.createUserPolicy(
       "resource-presentation-456-owner-policy",
       List.of(ownerId)
   );
   ```

3. **Creates Owner Permission**:
   ```java
   keycloakAuthzService.createPermission(
       "resource-presentation-456-permission",
       resourceId,
       List.of("read", "comment", "edit"),
       List.of(ownerPolicyId)
   );
   ```

### Step 7: Sharing Resources

**Via API** (`POST /api/resources/{resourceId}/share`):

```json
{
  "userIds": ["user-abc-123"],
  "permissionLevel": "read"
}
```

**Backend creates:**
1. User policy for `user-abc-123`
2. Permission with `read` scope
3. Links policy to resource

**Permission Levels:**
- `view` / `read`: Can only view the document
- `comment`: Can view and comment
- `edit`: Full edit access (but not owner)

### Step 8: Checking Permissions

The backend checks permissions before allowing access:

```java
boolean hasAccess = keycloakAuthzService.checkPermission(
    userId,
    "resource-presentation-456",
    "read"
);

if (!hasAccess) {
    throw new AppException(ErrorCode.FORBIDDEN);
}
```

### Step 9: Viewing Resources in Keycloak

**To view created resources:**
1. Go to **Clients** → **ai-primary** → **Authorization** tab
2. Click **Resources**
3. You'll see all registered resources (e.g., `resource-presentation-123`)

**To view policies:**
1. Click **Policies** sub-tab
2. You'll see owner policies, user policies, group policies

**To view permissions:**
1. Click **Permissions** sub-tab
2. You'll see all permissions linking resources to policies

### Resource Lifecycle

**1. Document Created:**
- Backend registers resource in Keycloak
- Creates owner policy and permission
- Owner has full access (read, comment, edit)

**2. Document Shared:**
- Backend creates user/group policy
- Creates permission with appropriate scopes
- Shared user can access with granted level

**3. Permission Updated:**
- Backend updates permission scopes
- E.g., upgrade from `read` to `edit`

**4. Access Revoked:**
- Backend deletes user policy and permission
- User loses access immediately

**5. Document Deleted:**
- Backend deletes all policies and permissions
- Deletes resource from Keycloak

### Troubleshooting Authorization

**Problem: User can't access shared document**

Check:
1. Resource exists in Keycloak:
   - Clients → ai-primary → Authorization → Resources
   - Search for `resource-{type}-{id}`

2. User policy exists:
   - Clients → ai-primary → Authorization → Policies
   - Search for `{resource-name}-user-{userId}-policy`

3. Permission exists with correct scopes:
   - Clients → ai-primary → Authorization → Permissions
   - Verify scopes include required action (read/comment/edit)

**Problem: Owner can't access their own document**

Check owner policy:
```bash
# Via Keycloak Admin API
GET /auth/admin/realms/ai-primary-dev/clients/{client-id}/authz/resource-server/policy?name={resource-name}-owner-policy
```

Ensure owner user ID is in the policy users list.

**Enable Authorization Debugging:**

In `application.yml`:
```yaml
logging:
  level:
    com.datn.datnbe.auth.service.KeycloakAuthorizationService: DEBUG
```

### Best Practices

1. **Always register resources** when creating documents
2. **Clean up resources** when documents are deleted
3. **Use consistent naming**: `resource-{type}-{id}`
4. **Batch operations**: Check multiple permissions in single call
5. **Cache permission results**: Avoid repeated Keycloak calls

---

## Testing the Setup

### 1. Test Keycloak Connection

```bash
# Health check
curl http://localhost:8082/health

# Get realm info
curl http://localhost:8082/realms/ai-primary-dev
```

### 2. Test OAuth2 Authorization Code Flow

#### Option A: Using Browser

1. Navigate to:
   ```
   http://localhost:8080/oauth2/authorization/keycloak
   ```
2. Login with your test user (admin/admin123)
3. You should be redirected back to the application

#### Option B: Using curl (Direct Access Grant)

```bash
# Get access token with username/password
curl -X POST 'http://localhost:8082/realms/ai-primary-dev/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=ai-primary' \
  -d 'client_secret=YOUR_CLIENT_SECRET' \
  -d 'grant_type=password' \
  -d 'username=admin' \
  -d 'password=admin123'
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "scope": "openid profile email"
}
```

### 3. Test Backend API with Token

```bash
# Set the token
TOKEN="<paste-access-token-from-above>"

# Test authenticated endpoint
curl -X GET 'http://localhost:8080/api/users/me' \
  -H "Authorization: Bearer $TOKEN"

# Test admin endpoint (should work with admin user)
curl -X GET 'http://localhost:8080/api/admin/users' \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Decode and Inspect JWT Token

Use [jwt.io](https://jwt.io) to decode the token and verify:
- **`realm_access.roles`** contains correct roles
- **`preferred_username`** is correct
- **`exp`** (expiration) is set properly
- **`iss`** matches your Keycloak realm URL

### 5. Test Role-Based Access Control

#### As Admin User

```bash
# Login as admin
TOKEN=$(curl -s -X POST 'http://localhost:8082/realms/ai-primary-dev/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=ai-primary' \
  -d 'client_secret=YOUR_CLIENT_SECRET' \
  -d 'grant_type=password' \
  -d 'username=admin' \
  -d 'password=admin123' | jq -r '.access_token')

# Should succeed (admin has access)
curl -X GET 'http://localhost:8080/api/admin/users' \
  -H "Authorization: Bearer $TOKEN"
```

#### As Regular User

```bash
# Login as regular user
TOKEN=$(curl -s -X POST 'http://localhost:8082/realms/ai-primary-dev/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=ai-primary' \
  -d 'client_secret=YOUR_CLIENT_SECRET' \
  -d 'grant_type=password' \
  -d 'username=testuser' \
  -d 'password=password123' | jq -r '.access_token')

# Should fail with 404 (access denied masked as not found)
curl -X GET 'http://localhost:8080/api/admin/users' \
  -H "Authorization: Bearer $TOKEN"

# Should succeed (user has access to user endpoints)
curl -X GET 'http://localhost:8080/api/users/me' \
  -H "Authorization: Bearer $TOKEN"
```

---

## Advanced Configuration

### Production Settings

When deploying to production, update these settings:

#### 1. Realm Settings

**Tokens Tab:**
- **Access Token Lifespan**: 5-15 minutes
- **SSO Session Idle**: 30 minutes
- **SSO Session Max**: 10 hours
- **Refresh Token Max Reuse**: 0 (security)

**Login Tab:**
- **Require SSL**: All requests
- **Email verification**: ON
- **Forgot password**: ON

**Security Defenses:**
- **Brute Force Detection**: ON
- **Max Login Failures**: 5
- **Wait Increment**: 60 seconds
- **Max Wait**: 900 seconds (15 minutes)

#### 2. Client Settings

**Valid Redirect URIs**: Restrict to your actual domains
```
https://yourdomain.com/*
https://api.yourdomain.com/login/oauth2/code/*
```

**Web Origins**: Restrict to your frontend domains
```
https://yourdomain.com
https://app.yourdomain.com
```

**Access Token Lifespan**: 5 minutes (override realm default if needed)

#### 3. SSL/TLS Configuration

For production, Keycloak must run behind HTTPS:

```yaml
# docker-compose.prod.yml
keycloak:
  environment:
    - KC_HOSTNAME=auth.yourdomain.com
    - KC_HOSTNAME_STRICT=true
    - KC_HOSTNAME_STRICT_HTTPS=true
    - KC_PROXY=edge
    - KC_HTTP_ENABLED=false
  # Use reverse proxy (nginx/traefik) for SSL termination
```

#### 4. Database Configuration

For production, use PostgreSQL instead of H2:

```yaml
keycloak:
  environment:
    - KC_DB=postgres
    - KC_DB_URL=jdbc:postgresql://postgres:5432/keycloak_db
    - KC_DB_USERNAME=keycloak
    - KC_DB_PASSWORD=secure-password
```

### Custom Theme (Optional)

To customize the Keycloak login page:

1. Create a custom theme
2. Mount it as a volume:
   ```yaml
   volumes:
     - ./keycloak-themes/mytheme:/opt/keycloak/themes/mytheme
   ```
3. Set theme in Realm Settings → Themes → Login theme

### Email Configuration

For password reset and email verification:

1. Go to **Realm Settings** → **Email**
2. Configure SMTP settings:
   ```
   Host: smtp.gmail.com
   Port: 587
   From: noreply@yourdomain.com
   Enable StartTLS: ON
   Authentication: ON
   Username: your-email@gmail.com
   Password: your-app-password
   ```
3. Test connection

### User Federation (LDAP/Active Directory)

To integrate with existing user directories:

1. Go to **User Federation**
2. Add provider (LDAP, Active Directory, etc.)
3. Configure connection settings
4. Map attributes and roles

### Social Login (Google, Facebook, etc.)

To enable social login:

1. Go to **Identity Providers**
2. Add provider (e.g., Google)
3. Configure OAuth2 credentials from Google Cloud Console
4. Map attributes to Keycloak user fields

---

## Troubleshooting

### Common Issues

#### 1. Client Secret Mismatch

**Error**: `invalid_client` or `Unauthorized`

**Solution**:
1. Go to Keycloak Admin → Clients → ai-primary → Credentials
2. Copy the actual secret
3. Update `.env`:
   ```bash
   KEYCLOAK_CLIENT_SECRET=<correct-secret>
   ```
4. Restart backend:
   ```bash
   ./gradlew bootRun
   ```

#### 2. Invalid Redirect URI

**Error**: `Invalid redirect uri` or `invalid_redirect_uri`

**Solution**:
1. Check your backend logs for the actual redirect URI being used
2. Add it to Keycloak:
   - Clients → ai-primary → Settings → Valid Redirect URIs
   - Add the URI (e.g., `http://localhost:8080/login/oauth2/code/keycloak`)
   - Click **Save**

#### 3. CORS Issues

**Error**: `CORS policy blocked` in browser console

**Solution**:
1. Add frontend URL to Keycloak:
   - Clients → ai-primary → Settings → Web Origins
   - Add your frontend URL (e.g., `http://localhost:5173`)
   - Or use `*` for development (NOT recommended for production)
2. Ensure backend CORS is configured in `application.yml`

#### 4. Token Validation Failed

**Error**: `Invalid token` or `JWT signature validation failed`

**Possible causes:**
- Issuer URI mismatch
- Token expired
- Clock skew between systems

**Solution**:
1. Verify issuer URI in `.env` matches Keycloak:
   ```bash
   KEYCLOAK_ISSUER_URI=http://localhost:8082/realms/ai-primary-dev
   ```
2. Check token expiration (default is 5 minutes)
3. Sync system clocks if needed

#### 5. Roles Not Found in Token

**Error**: User has roles in Keycloak but backend doesn't see them

**Solution**:
1. Verify role mapping:
   - Users → [username] → Role mapping
   - Ensure roles are assigned
2. Check client scope:
   - Clients → ai-primary → Client scopes
   - Ensure `roles` scope is included
3. Inspect JWT at [jwt.io](https://jwt.io):
   - Check if `realm_access.roles` contains the roles
   - If not, check Client Scopes → roles → Mappers

#### 6. Keycloak Container Won't Start

**Error**: Container exits immediately

**Solutions**:
```bash
# Check logs
docker logs keycloak

# Common issues:
# - Port 8082 already in use
lsof -i :8082
kill -9 <PID>

# - Database connection failed (if using PostgreSQL)
# Ensure database is running and accessible

# - Volume permission issues
sudo chown -R 1000:1000 keycloak-data/
```

#### 7. Access Denied (403) on Protected Endpoints

**Error**: `Access Denied` or returns 404 (masked as not found)

**Solution**:
1. Verify user has correct role:
   - Users → [username] → Role mapping
   - Add required role (admin or user)
2. Check SecurityConfig.java for endpoint rules
3. Verify JWT contains roles:
   ```bash
   # Decode token at jwt.io
   # Check realm_access.roles array
   ```

#### 8. Session Expired Too Quickly

**Error**: User logged out unexpectedly

**Solution**:
1. Increase session timeout:
   - Realm Settings → Tokens
   - **SSO Session Idle**: 30 minutes (or more)
   - **SSO Session Max**: 10 hours
2. Consider using refresh tokens in frontend

### Debug Mode

Enable debug logging for Keycloak-related issues:

**Backend (application.yml):**
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
```

**Restart and check logs:**
```bash
./gradlew bootRun
```

Look for JWT validation errors, role extraction issues, etc.

### Useful Keycloak Admin API Endpoints

```bash
# Get realm info
curl http://localhost:8082/realms/ai-primary-dev

# Get OpenID configuration
curl http://localhost:8082/realms/ai-primary-dev/.well-known/openid-configuration

# Get public keys for JWT verification
curl http://localhost:8082/realms/ai-primary-dev/protocol/openid-connect/certs
```

---

## Summary

### Quick Checklist

- ✅ Keycloak running on port 8082
- ✅ Realm `ai-primary-dev` created
- ✅ Client `ai-primary` configured
- ✅ Roles `admin` and `user` created
- ✅ Test users created with roles assigned
- ✅ Client secret added to `.env`
- ✅ Backend can validate JWT tokens
- ✅ Role-based access control working

### Key Configuration Files

| File                                    | Purpose                              |
|-----------------------------------------|--------------------------------------|
| `docker-compose.yml`                    | Keycloak container setup             |
| `keycloak-data/ai-primary-dev-realm.json` | Realm export (backup)              |
| `.env`                                  | Keycloak connection settings         |
| `src/.../SecurityConfig.java`           | Spring Security configuration        |
| `src/.../JwtConverter.java`             | JWT to Spring Security role mapping  |
| `src/main/resources/application.yml`    | OAuth2 client configuration          |

### Next Steps

1. ✅ Configure Keycloak (done!)
2. 📖 Test authentication flow
3. 🔐 Create production users
4. 🚀 Deploy to production with SSL/HTTPS
5. 📊 Monitor authentication metrics
6. 🔄 Set up refresh token rotation (if needed)
7. 📧 Configure email for password reset

---

## Additional Resources

- **Keycloak Documentation**: https://www.keycloak.org/documentation
- **Spring Security OAuth2**: https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html
- **OpenID Connect Spec**: https://openid.net/specs/openid-connect-core-1_0.html
- **JWT.io**: https://jwt.io (for decoding tokens)
- **Keycloak Admin REST API**: https://www.keycloak.org/docs-api/latest/rest-api/index.html

---

**Need Help?**

If you encounter issues not covered here:
1. Check Keycloak logs: `docker logs keycloak`
2. Check backend logs: `./gradlew bootRun`
3. Inspect JWT token at [jwt.io](https://jwt.io)
4. Verify realm/client configuration in Keycloak Admin Console

**Happy authenticating! 🔐**
