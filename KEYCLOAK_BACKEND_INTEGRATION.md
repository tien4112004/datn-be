# Keycloak Backend Integration Guide

This document explains how the DATN Backend application integrates with Keycloak for authentication and authorization.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Authentication Flows](#authentication-flows)
  - [Login (Username/Password)](#login-usernamepassword)
  - [Signup (User Registration)](#signup-user-registration)
  - [Login with Google](#login-with-google)
  - [Logout](#logout)
- [Authorization (Resource Permissions)](#authorization-resource-permissions)
  - [Register Resource](#register-resource)
  - [Share Resource](#share-resource)
  - [Check Permission](#check-permission)
- [Code Examples](#code-examples)
- [API Reference](#api-reference)

---

## Architecture Overview

### Components

```
Frontend (Web/Mobile)
        ↓
Backend (Spring Boot + Spring Security)
        ↓
Keycloak (Authentication & Authorization Server)
```

### Key Services

| Service | Purpose | Location |
|---------|---------|----------|
| `KeycloakAuthService` | User management, login, signup | `auth/service/` |
| `OAuthCallbackService` | Google OAuth callback handling | `auth/service/` |
| `KeycloakAuthorizationService` | Resource permissions (UMA) | `auth/service/` |
| `AuthenticationService` | Authentication orchestration | `auth/service/` |
| `SessionManagementService` | Cookie/session management | `auth/service/` |

### Authentication Methods

1. **Direct Authentication**: Username/password → Keycloak token endpoint
2. **OAuth2 Authorization Code Flow**: Google login → Keycloak broker
3. **JWT Token Validation**: Spring Security validates Keycloak JWTs

---

## Authentication Flows

### Login (Username/Password)

**Endpoint**: `POST /api/auth/signin`

**Flow:**

```
1. User submits credentials
   ↓
2. Backend calls Keycloak token endpoint
   ↓
3. Keycloak validates credentials
   ↓
4. Returns access_token + refresh_token
   ↓
5. Backend creates session cookie
   ↓
6. Returns tokens to frontend
```

**Code Implementation:**

```java
// AuthController.java
@PostMapping("/signin")
public ResponseEntity<AppResponseDto<SignInResponse>> signIn(
    @Valid @RequestBody SigninRequest request,
    HttpServletResponse response
) {
    // Step 1: Authenticate with Keycloak
    SignInResponse signInResponse = authenticationService.signIn(request);

    // Step 2: Create session cookie
    sessionService.createSession(response, signInResponse);

    // Step 3: Return tokens
    return ResponseEntity.ok(AppResponseDto.success(signInResponse));
}
```

**Keycloak Token Request:**

```java
// KeycloakAuthService.java
public AuthTokenResponse authenticate(String username, String password) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "password");
    formData.add("client_id", authProperties.getClientId());
    formData.add("client_secret", authProperties.getClientSecret());
    formData.add("username", username);
    formData.add("password", password);

    // POST to Keycloak token endpoint
    ResponseEntity<AuthTokenResponse> response = restTemplate.postForEntity(
        authProperties.getTokenUri(),
        new HttpEntity<>(formData, headers),
        AuthTokenResponse.class
    );

    return response.getBody();
}
```

**Keycloak Token Endpoint:**
```
POST http://keycloak:8080/realms/ai-primary-dev/protocol/openid-connect/token

Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=ai-primary
&client_secret=your-secret
&username=user@example.com
&password=password123
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "scope": "openid profile email"
}
```

---

### Signup (User Registration)

**Endpoint**: `POST /api/auth/signup`

**Flow:**

```
1. User submits registration form
   ↓
2. Backend creates user in Keycloak
   ↓
3. Keycloak assigns default "user" role
   ↓
4. Backend creates user profile in database
   ↓
5. Returns user profile
```

**Code Implementation:**

```java
// AuthController.java
@PostMapping("/signup")
public ResponseEntity<AppResponseDto<UserProfileResponse>> signup(
    @Valid @RequestBody SignupRequest request
) {
    // Creates Keycloak user + local user profile
    UserProfileResponse response = userProfileApi.createUserProfile(request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(AppResponseDto.success(response));
}
```

**Keycloak User Creation:**

```java
// KeycloakAuthService.java
public String createKeycloakUser(
    String username,
    String password,
    String firstName,
    String lastName,
    String role
) {
    // 1. Create user representation
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(username);
    user.setEmail(username.contains("@") ? username : null);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmailVerified(true);

    // 2. Set password
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(password);
    credential.setTemporary(false);
    user.setCredentials(Collections.singletonList(credential));

    // 3. Create user via Keycloak Admin API
    Response response = usersResource.create(user);

    // 4. Extract user ID from Location header
    String keycloakUserId = extractUserIdFromLocation(
        response.getHeaderString("Location")
    );

    // 5. Assign realm role
    assignRealmRole(keycloakUserId, role.toLowerCase());

    return keycloakUserId;
}
```

**Keycloak Admin API Call:**
```java
// Using Keycloak Admin Client
POST /auth/admin/realms/ai-primary-dev/users

Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "emailVerified": true,
  "credentials": [{
    "type": "password",
    "value": "password123",
    "temporary": false
  }]
}
```

---

### Login with Google

**Endpoint**: `GET /api/auth/google/authorize` → Callback: `GET /api/auth/google/callback`

**Flow:**

```
1. User clicks "Login with Google"
   ↓
2. Frontend calls /api/auth/google/authorize
   ↓
3. Backend generates Keycloak authorization URL
   ↓
4. Redirects to Keycloak
   ↓
5. Keycloak redirects to Google OAuth
   ↓
6. User authenticates with Google
   ↓
7. Google redirects back to Keycloak
   ↓
8. Keycloak creates/updates user (via broker)
   ↓
9. Keycloak redirects to backend callback
   ↓
10. Backend exchanges code for tokens
   ↓
11. Backend syncs user to local database
   ↓
12. Redirects to frontend with tokens
```

**Code Implementation:**

**Step 1: Generate Authorization URL**

```java
// AuthController.java
@GetMapping("/google/authorize")
public void googleAuthorize(
    @RequestParam(defaultValue = "web") String clientType,
    HttpServletResponse response
) throws IOException {
    // Generate Keycloak authorization URL with Google IDP
    String googleLoginUrl = oauthCallbackService.generateGoogleLoginUrl(clientType);

    response.sendRedirect(googleLoginUrl);
}
```

```java
// OAuthCallbackService.java
public String generateGoogleLoginUrl(String clientType) {
    // State parameter to track client type (web/mobile)
    String state = clientType + ":" + UUID.randomUUID();

    // Build Keycloak authorization URL
    return UriComponentsBuilder
        .fromHttpUrl(authProperties.getIssuer())
        .path("/protocol/openid-connect/auth")
        .queryParam("client_id", authProperties.getClientId())
        .queryParam("redirect_uri", authProperties.getGoogleCallbackUri())
        .queryParam("response_type", "code")
        .queryParam("scope", "openid profile email")
        .queryParam("state", state)
        .queryParam("kc_idp_hint", "google")  // Bypass Keycloak login, go directly to Google
        .build()
        .toUriString();
}
```

**Generated URL:**
```
http://keycloak:8080/realms/ai-primary-dev/protocol/openid-connect/auth
  ?client_id=ai-primary
  &redirect_uri=http://backend:8080/api/auth/google/callback
  &response_type=code
  &scope=openid+profile+email
  &state=web:abc-123-def
  &kc_idp_hint=google
```

**Step 2: Handle Callback**

```java
// AuthController.java
@GetMapping("/google/callback")
public void googleCallback(
    @RequestParam String code,
    @RequestParam String state,
    HttpServletResponse response
) throws IOException {
    // 1. Parse client type from state
    String clientType = extractClientType(state);

    // 2. Exchange authorization code for tokens
    KeycloakCallbackRequest request = KeycloakCallbackRequest.builder()
        .code(code)
        .redirectUri(authProperties.getGoogleCallbackUri())
        .build();

    SignInResponse tokens = oauthCallbackService.processCallback(request);

    // 3. Create session
    sessionService.createSession(response, tokens);

    // 4. Redirect to frontend
    String feUrl = clientType.equals("mobile")
        ? authProperties.getMobileRedirectUrl()
        : authProperties.getFeRedirectUrl();

    response.sendRedirect(feUrl + "/auth/google/callback");
}
```

**Step 3: Exchange Code for Tokens**

```java
// KeycloakAuthService.java
public AuthTokenResponse exchangeAuthorizationCode(KeycloakCallbackRequest request) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("grant_type", "authorization_code");
    formData.add("client_id", authProperties.getClientId());
    formData.add("client_secret", authProperties.getClientSecret());
    formData.add("code", request.getCode());
    formData.add("redirect_uri", request.getRedirectUri());

    // POST to Keycloak token endpoint
    return restTemplate.postForEntity(
        authProperties.getTokenUri(),
        new HttpEntity<>(formData, headers),
        AuthTokenResponse.class
    ).getBody();
}
```

**Keycloak Token Exchange:**
```
POST http://keycloak:8080/realms/ai-primary-dev/protocol/openid-connect/token

grant_type=authorization_code
&client_id=ai-primary
&client_secret=your-secret
&code=AUTH_CODE_FROM_CALLBACK
&redirect_uri=http://backend:8080/api/auth/google/callback
```

**Step 4: Sync User to Local Database**

```java
// OAuthCallbackService.java
public SignInResponse processCallback(KeycloakCallbackRequest request) {
    // 1. Exchange code for tokens
    AuthTokenResponse tokens = keycloakAuthService.exchangeAuthorizationCode(request);

    // 2. Decode JWT to get user info
    Jwt jwt = jwtDecoder.decode(tokens.getAccessToken());
    String keycloakUserId = jwt.getSubject();
    String email = jwt.getClaimAsString("email");
    String givenName = jwt.getClaimAsString("given_name");
    String familyName = jwt.getClaimAsString("family_name");

    // 3. Create/update user in local database
    userProfileApi.createUserFromKeycloakUser(
        keycloakUserId,
        email,
        givenName,
        familyName
    );

    // 4. Return tokens
    return SignInResponse.builder()
        .accessToken(tokens.getAccessToken())
        .refreshToken(tokens.getRefreshToken())
        .build();
}
```

---

### Logout

**Endpoint**: `POST /api/auth/logout`

**Flow:**

```
1. User clicks logout
   ↓
2. Backend calls Keycloak logout endpoint
   ↓
3. Keycloak invalidates tokens
   ↓
4. Backend clears session cookie
   ↓
5. Returns success
```

**Code Implementation:**

```java
// AuthController.java
@PostMapping("/logout")
public ResponseEntity<AppResponseDto<Map<String, String>>> logout(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication auth
) {
    try {
        // Logout from Keycloak
        authenticationService.logout(request, response, auth);
    } finally {
        // Clear session cookie
        sessionService.clearSession(response);
    }

    return ResponseEntity.noContent().build();
}
```

```java
// AuthenticationService.java
public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
    // Get refresh token from cookie
    String refreshToken = extractRefreshTokenFromCookie(request);

    if (refreshToken != null) {
        // Call Keycloak logout endpoint
        keycloakAuthService.logout(refreshToken);
    }
}
```

**Keycloak Logout Request:**

```java
// KeycloakAuthService.java
public void logout(String refreshToken) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", authProperties.getClientId());
    formData.add("client_secret", authProperties.getClientSecret());
    formData.add("refresh_token", refreshToken);

    // POST to Keycloak logout endpoint
    restTemplate.postForEntity(
        authProperties.getLogoutUri(),
        new HttpEntity<>(formData, headers),
        Void.class
    );
}
```

**Keycloak Logout Endpoint:**
```
POST http://keycloak:8080/realms/ai-primary-dev/protocol/openid-connect/logout

client_id=ai-primary
&client_secret=your-secret
&refresh_token=REFRESH_TOKEN
```

---

## Authorization (Resource Permissions)

The application uses **Keycloak Authorization Services** (UMA 2.0) for fine-grained resource permissions.

### Concepts

- **Resource**: A document (presentation, assignment, etc.)
- **Scopes**: Actions on resources (`read`, `comment`, `edit`)
- **Policy**: Who can access (owner, specific users, groups)
- **Permission**: Links resources + scopes + policies

### Register Resource

**When**: Document is created

**Purpose**: Register document in Keycloak so permissions can be managed

**Code Implementation:**

```java
// ResourcePermissionManagement.java
@Override
public DocumentRegistrationResponse registerResource(ResourceRegistrationRequest request) {
    String ownerId = securityContextUtils.getCurrentUserId();

    // 1. Create resource in Keycloak
    KeycloakResourceDto resource = keycloakAuthzService.createResource(
        "resource-" + request.getResourceType() + "-" + request.getResourceId(),
        request.getResourceName(),
        "/api/" + request.getResourceType() + "s/" + request.getResourceId(),
        ownerId
    );

    // 2. Create owner policy
    String ownerPolicyName = resource.getName() + "-owner-policy";
    KeycloakUserPolicyDto ownerPolicy = keycloakAuthzService.createUserPolicy(
        ownerPolicyName,
        "Owner policy for " + request.getResourceName(),
        List.of(ownerId)
    );

    // 3. Create owner permission (all scopes: read, comment, edit)
    String permissionName = resource.getName() + "-permission";
    keycloakAuthzService.createPermission(
        permissionName,
        "Owner permissions for " + request.getResourceName(),
        resource.getId(),
        List.of("read", "comment", "edit"),
        List.of(ownerPolicy.getId())
    );

    // 4. Save mapping to database
    DocumentResourceMapping mapping = DocumentResourceMapping.builder()
        .documentId(request.getResourceId())
        .resourceId(resource.getId())
        .resourceType(request.getResourceType())
        .ownerId(ownerId)
        .build();

    mappingRepository.save(mapping);

    return DocumentRegistrationResponse.builder()
        .resourceId(resource.getId())
        .resourceName(resource.getName())
        .build();
}
```

**Keycloak API Calls:**

```java
// KeycloakAuthorizationService.java

// 1. Create Resource
public KeycloakResourceDto createResource(String name, String displayName, String uri, String ownerId) {
    Map<String, Object> resourceData = Map.of(
        "name", name,
        "displayName", displayName,
        "uris", List.of(uri),
        "type", "urn:datn-be:resources:document",
        "scopes", List.of(
            Map.of("name", "read"),
            Map.of("name", "comment"),
            Map.of("name", "edit")
        ),
        "owner", ownerId,
        "ownerManagedAccess", false
    );

    return webClient.post()
        .uri("/admin/realms/{realm}/clients/{clientId}/authz/resource-server/resource",
             realmName, clientUuid)
        .header("Authorization", "Bearer " + getAdminToken())
        .bodyValue(resourceData)
        .retrieve()
        .bodyToMono(KeycloakResourceDto.class)
        .block();
}

// 2. Create User Policy
public KeycloakUserPolicyDto createUserPolicy(String name, String description, List<String> userIds) {
    Map<String, Object> policyData = Map.of(
        "type", "user",
        "name", name,
        "description", description,
        "logic", "POSITIVE",
        "users", userIds
    );

    return webClient.post()
        .uri("/admin/realms/{realm}/clients/{clientId}/authz/resource-server/policy/user",
             realmName, clientUuid)
        .header("Authorization", "Bearer " + getAdminToken())
        .bodyValue(policyData)
        .retrieve()
        .bodyToMono(KeycloakUserPolicyDto.class)
        .block();
}

// 3. Create Permission
public KeycloakPermissionDto createPermission(
    String name,
    String description,
    String resourceId,
    List<String> scopes,
    List<String> policyIds
) {
    Map<String, Object> permissionData = Map.of(
        "type", "scope",
        "name", name,
        "description", description,
        "decisionStrategy", "UNANIMOUS",
        "resources", List.of(resourceId),
        "scopes", scopes,
        "policies", policyIds
    );

    return webClient.post()
        .uri("/admin/realms/{realm}/clients/{clientId}/authz/resource-server/permission/scope",
             realmName, clientUuid)
        .header("Authorization", "Bearer " + getAdminToken())
        .bodyValue(permissionData)
        .retrieve()
        .bodyToMono(KeycloakPermissionDto.class)
        .block();
}
```

### Share Resource

**When**: Owner shares document with other users

**Purpose**: Grant specific permission level to users

**Code Implementation:**

```java
// ResourcePermissionManagement.java
@Override
public ResourceShareResponse shareResource(String resourceId, ResourceShareRequest request) {
    // 1. Get resource mapping
    DocumentResourceMapping mapping = getResourceMapping(resourceId);

    // 2. Get current user (must be owner)
    String currentUserId = securityContextUtils.getCurrentUserId();
    if (!mapping.getOwnerId().equals(currentUserId)) {
        throw new AppException(ErrorCode.FORBIDDEN);
    }

    // 3. For each user to share with
    for (String userId : request.getUserIds()) {
        // Create user policy
        String policyName = mapping.getResourceName() + "-user-" + userId + "-policy";
        KeycloakUserPolicyDto policy = keycloakAuthzService.createUserPolicy(
            policyName,
            request.getPermissionLevel() + " for " + mapping.getResourceName(),
            List.of(userId)
        );

        // Determine scopes based on permission level
        List<String> scopes = getScopesForLevel(request.getPermissionLevel());

        // Create permission
        String permissionName = mapping.getResourceName() + "-user-" + userId
            + "-" + request.getPermissionLevel() + "-permission";

        keycloakAuthzService.createPermission(
            permissionName,
            request.getPermissionLevel() + " permissions for " + mapping.getResourceName(),
            mapping.getResourceId(),
            scopes,
            List.of(policy.getId())
        );
    }

    // 4. Send notifications to shared users
    sendShareNotifications(request.getUserIds(), mapping);

    return ResourceShareResponse.builder()
        .sharedWith(request.getUserIds())
        .permissionLevel(request.getPermissionLevel())
        .build();
}

private List<String> getScopesForLevel(String level) {
    return switch (level.toLowerCase()) {
        case "read", "view" -> List.of("read");
        case "comment" -> List.of("read", "comment");
        case "edit" -> List.of("read", "comment", "edit");
        default -> List.of("read");
    };
}
```

**Permission Levels:**

| Level | Scopes | Description |
|-------|--------|-------------|
| `read` / `view` | `read` | Can only view |
| `comment` | `read`, `comment` | Can view and comment |
| `edit` | `read`, `comment`, `edit` | Full edit access |

### Check Permission

**When**: Before allowing access to a resource

**Purpose**: Verify user has permission to perform action

**Code Implementation:**

**Method 1: Using Keycloak Authorization Services**

```java
// KeycloakAuthorizationService.java
public boolean checkPermission(String userId, String resourceName, String scope) {
    try {
        // 1. Get user access token
        String userToken = getUserAccessToken(userId);

        // 2. Request permission ticket from Keycloak
        Map<String, Object> request = Map.of(
            "grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket",
            "audience", clientId,
            "permission", resourceName + "#" + scope
        );

        // 3. POST to token endpoint
        ResponseEntity<Map> response = restTemplate.postForEntity(
            tokenUri,
            new HttpEntity<>(request, headers),
            Map.class
        );

        // If no exception, user has permission
        return response.getStatusCode() == HttpStatus.OK;

    } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
        // Permission denied
        return false;
    }
}
```

**Method 2: Direct Permission Evaluation**

```java
// KeycloakAuthorizationService.java
public boolean evaluatePermission(String userId, String resourceId, String scope) {
    // Build evaluation request
    Map<String, Object> evaluationRequest = Map.of(
        "userId", userId,
        "resources", List.of(Map.of(
            "id", resourceId,
            "scopes", List.of(scope)
        ))
    );

    // Call Keycloak policy evaluation API
    ResponseEntity<Map> response = webClient.post()
        .uri("/admin/realms/{realm}/clients/{clientId}/authz/resource-server/policy/evaluate",
             realmName, clientUuid)
        .header("Authorization", "Bearer " + getAdminToken())
        .bodyValue(evaluationRequest)
        .retrieve()
        .toEntity(Map.class)
        .block();

    // Parse evaluation result
    List<Map> results = (List<Map>) response.getBody().get("results");
    return results.stream()
        .anyMatch(r -> "PERMIT".equals(r.get("status")));
}
```

**Usage in Service Layer:**

```java
// PresentationService.java
@Service
public class PresentationService {
    private final KeycloakAuthorizationService authzService;
    private final PresentationRepository repository;

    public PresentationResponse getPresentation(String presentationId) {
        String userId = SecurityContextUtils.getCurrentUserId();

        // Check if user has read permission
        String resourceName = "resource-presentation-" + presentationId;
        boolean hasAccess = authzService.checkPermission(userId, resourceName, "read");

        if (!hasAccess) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // User has permission, proceed
        Presentation presentation = repository.findById(presentationId)
            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return mapper.toResponse(presentation);
    }
}
```

**Using Custom Annotation (Actual Implementation):**

The application uses a custom `@RequireDocumentPermission` annotation with AOP aspect for permission checking.

```java
// PresentationController.java
@RestController
@RequestMapping("/api/presentations")
public class PresentationController {

    @GetMapping("/{id}")
    @RequireDocumentPermission(scopes = {"read"}, documentIdParam = "id")
    public ResponseEntity<PresentationResponse> getPresentation(@PathVariable String id) {
        // Permission checked automatically by aspect before method execution
        PresentationResponse response = service.getPresentation(id);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PutMapping("/{id}")
    @RequireDocumentPermission(scopes = {"edit"}, documentIdParam = "id")
    public ResponseEntity<PresentationResponse> updatePresentation(
        @PathVariable String id,
        @RequestBody UpdatePresentationRequest request
    ) {
        // Edit permission required - checked by aspect
        PresentationResponse response = service.updatePresentation(id, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/{id}/comments")
    @RequireDocumentPermission(scopes = {"comment"}, documentIdParam = "id")
    public ResponseEntity<CommentResponse> addComment(
        @PathVariable String id,
        @RequestBody CreateCommentRequest request
    ) {
        // Comment permission required
        CommentResponse response = service.addComment(id, request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
```

**How it works:**

1. **Annotation Definition:**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireDocumentPermission {
    String[] scopes() default {"read"};
    String documentIdParam() default "id";
    boolean throwOnFail() default true;
}
```

2. **AOP Aspect (`DocumentPermissionAspect`):**
   - Intercepts methods with `@RequireDocumentPermission`
   - Extracts document ID from method parameters
   - Calls `ResourcePermissionManagement.checkUserPermissions(documentId, userId)`
   - Validates user has required scopes (read, comment, edit)
   - Throws `AppException(ErrorCode.FORBIDDEN)` if permission denied

3. **Permission Hierarchy:**
```java
// If user has "edit", they automatically have "read" and "comment"
// If user has "comment", they automatically have "read"
// Hierarchy: edit > comment > read
```

4. **Enable/Disable:**
```yaml
# application.yml
app:
  security:
    document-permission:
      enabled: true  # Set to false to disable permission checks
```

---

## Code Examples

### Complete Login Flow

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authService;
    private final SessionManagementService sessionService;

    @PostMapping("/signin")
    public ResponseEntity<AppResponseDto<SignInResponse>> signIn(
        @Valid @RequestBody SigninRequest request,
        HttpServletResponse response
    ) {
        // 1. Authenticate with Keycloak (username/password)
        SignInResponse signInResponse = authService.signIn(request);

        // 2. Create HTTP-only session cookie
        sessionService.createSession(response, signInResponse);

        // 3. Return tokens
        return ResponseEntity.ok(AppResponseDto.success(signInResponse));
    }
}

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final KeycloakAuthService keycloakAuthService;

    public SignInResponse signIn(SigninRequest request) {
        // Call Keycloak token endpoint
        AuthTokenResponse tokens = keycloakAuthService.authenticate(
            request.getAccount(),
            request.getPassword()
        );

        return SignInResponse.builder()
            .accessToken(tokens.getAccessToken())
            .refreshToken(tokens.getRefreshToken())
            .tokenType(tokens.getTokenType())
            .expiresIn(tokens.getExpiresIn())
            .build();
    }
}
```

### Complete Signup Flow

```java
@Service
@RequiredArgsConstructor
public class UserManagement implements UserProfileApi {
    private final KeycloakAuthService keycloakAuthService;
    private final UserProfileRepo userProfileRepo;

    @Override
    @Transactional
    public UserProfileResponse createUserProfile(SignupRequest request) {
        // 1. Create user in Keycloak
        String keycloakUserId = keycloakAuthService.createKeycloakUser(
            request.getAccount(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            "user"  // Default role
        );

        // 2. Create user profile in our database
        UserProfile userProfile = UserProfile.builder()
            .keycloakUserId(keycloakUserId)
            .username(request.getAccount())
            .email(request.getAccount())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();

        UserProfile saved = userProfileRepo.save(userProfile);

        // 3. Return response
        return mapper.toResponse(saved);
    }
}
```

### Complete Resource Registration Flow

```java
@Service
@RequiredArgsConstructor
public class PresentationService {
    private final ResourcePermissionApi resourcePermissionApi;
    private final PresentationRepository repository;

    @Transactional
    public PresentationResponse createPresentation(CreatePresentationRequest request) {
        // 1. Create presentation in database
        Presentation presentation = Presentation.builder()
            .title(request.getTitle())
            .ownerId(SecurityContextUtils.getCurrentUserId())
            .build();

        Presentation saved = repository.save(presentation);

        // 2. Register resource in Keycloak
        ResourceRegistrationRequest resourceRequest = ResourceRegistrationRequest.builder()
            .resourceId(saved.getId())
            .resourceType("presentation")
            .resourceName(saved.getTitle())
            .build();

        resourcePermissionApi.registerResource(resourceRequest);

        // 3. Return response
        return mapper.toResponse(saved);
    }
}
```

---

## API Reference

### Keycloak Endpoints Used

| Endpoint | Purpose | Method |
|----------|---------|--------|
| `/protocol/openid-connect/token` | Get access token | POST |
| `/protocol/openid-connect/logout` | Logout | POST |
| `/protocol/openid-connect/auth` | OAuth2 authorization | GET |
| `/admin/realms/{realm}/users` | Create user | POST |
| `/admin/realms/{realm}/clients/{id}/authz/resource-server/resource` | Create resource | POST |
| `/admin/realms/{realm}/clients/{id}/authz/resource-server/policy/user` | Create user policy | POST |
| `/admin/realms/{realm}/clients/{id}/authz/resource-server/permission/scope` | Create permission | POST |

### Backend API Endpoints

| Endpoint | Purpose | Auth Required |
|----------|---------|---------------|
| `POST /api/auth/signin` | Login with username/password | No |
| `POST /api/auth/signup` | Register new user | No |
| `POST /api/auth/logout` | Logout | Yes |
| `GET /api/auth/google/authorize` | Initiate Google login | No |
| `GET /api/auth/google/callback` | Google OAuth callback | No |
| `POST /api/resources/register` | Register resource | Yes |
| `POST /api/resources/{id}/share` | Share resource | Yes (owner) |
| `GET /api/resources/{id}/permissions` | Get resource permissions | Yes |

### Environment Variables

```bash
# Keycloak Configuration
KEYCLOAK_CLIENT_ID=ai-primary
KEYCLOAK_CLIENT_SECRET=your-secret
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/ai-primary-dev
KEYCLOAK_SERVER_URL=http://keycloak:8080
KEYCLOAK_REALM_NAME=ai-primary-dev

# OAuth Callback URIs
GOOGLE_CALLBACK_URI=http://backend:8080/api/auth/google/callback
FE_REDIRECT_URI=http://localhost:5173/auth/google/callback
MOBILE_REDIRECT_URI=aiprimary://auth-callback
```

---

## Summary

**Authentication:**
- ✅ Username/Password login via Keycloak token endpoint
- ✅ User registration via Keycloak Admin API
- ✅ Google OAuth via Keycloak Identity Provider
- ✅ JWT token validation via Spring Security
- ✅ Session management with HTTP-only cookies

**Authorization:**
- ✅ Resource registration in Keycloak
- ✅ Fine-grained permissions (read, comment, edit)
- ✅ User-based and group-based policies
- ✅ Permission checking before resource access
- ✅ Automatic cleanup on resource deletion

**Key Integration Points:**
1. **Keycloak Token Endpoint**: Authentication
2. **Keycloak Admin API**: User management
3. **Keycloak Authorization Services**: Resource permissions
4. **Spring Security**: JWT validation and enforcement
5. **Keycloak Identity Providers**: Social login (Google)
