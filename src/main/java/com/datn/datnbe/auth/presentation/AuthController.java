package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import com.datn.datnbe.auth.dto.response.SignInResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.service.AuthenticationService;
import com.datn.datnbe.auth.service.KeycloakAuthService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {
    AuthProperties authProperties;
    UserProfileApi userProfileApi;
    AuthenticationService authenticationService;
    KeycloakAuthService keycloakAuthService;
    Integer MAX_AGE = 604800; // 7 days

    @PostMapping("/signin")
    public ResponseEntity<AppResponseDto<SignInResponse>> signIn(@Valid @RequestBody SigninRequest request,
            HttpServletResponse response) {
        SignInResponse signInResponse = authenticationService.signIn(request);

        Cookie accessTokenCookie = createCookie("access_token",
                signInResponse.getAccessToken(),
                signInResponse.getExpiresIn());
        Cookie refreshTokenCookie = createCookie("refresh_token", signInResponse.getRefreshToken(), MAX_AGE);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(AppResponseDto.success(signInResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserProfileResponse response = userProfileApi.createUserProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {

        new SecurityContextLogoutHandler().logout(request, response, auth);

        String idToken = null;
        if (auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
            idToken = oidc.getIdToken().getTokenValue();
        }

        String url = UriComponentsBuilder.fromUriString(authProperties.getLogoutUri())
                .queryParam("post_logout_redirect_uri", authProperties.getRedirectUri())
                .queryParam("id_token_hint", idToken)
                .toUriString();

        return "redirect:" + url;
    }

    @PostMapping("/exchange")
    public ResponseEntity<AppResponseDto<SignInResponse>> keycloakCallback(
            @Valid @RequestBody KeycloakCallbackRequest request,
            HttpServletResponse response) {

        // Exchange authorization code for tokens using existing KeycloakAuthService
        AuthTokenResponse authTokenResponse = keycloakAuthService.exchangeAuthorizationCode(request);

        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
        Jwt jwt = decoder.decode(authTokenResponse.getAccessToken());

        // sync user profile if not exists
        userProfileApi.createUserFromKeycloakUser(jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"));

        // Add cookies to response
        Cookie accessTokenCookie = createCookie("access_token",
                authTokenResponse.getAccessToken(),
                authTokenResponse.getExpiresIn());
        Cookie refreshTokenCookie = createCookie("refresh_token", authTokenResponse.getRefreshToken(), MAX_AGE);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // Map AuthTokenResponse to SignInResponse
        SignInResponse signInResponse = SignInResponse.builder()
                .accessToken(authTokenResponse.getAccessToken())
                .refreshToken(authTokenResponse.getRefreshToken())
                .tokenType(authTokenResponse.getTokenType())
                .expiresIn(authTokenResponse.getExpiresIn())
                .build();

        return ResponseEntity.ok(AppResponseDto.success(signInResponse));
    }

    @GetMapping("/google/authorize")
    public void googleAuthorize(@RequestParam(required = false, defaultValue = "web") String clientType,
            HttpServletResponse response) throws IOException {
        // Encode client type in state parameter to track where to redirect after authentication
        String state = clientType + ":" + UUID.randomUUID().toString();

        Map<String, String> params = Map.of("client_id",
                authProperties.getClientId(),
                "response_type",
                "code",
                "scope",
                "openid profile email",
                "redirect_uri",
                authProperties.getGoogleCallbackUri(),
                "state",
                state,
                "kc_idp_hint",
                "google",
                "prompt",
                "login");

        String queryString = params.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String googleLoginUrl = String.format("%s/realms/%s/protocol/openid-connect/auth?%s",
                authProperties.getServerUrl(),
                authProperties.getRealm(),
                queryString);

        log.info("Redirecting to Google login with clientType: {}", clientType);
        response.sendRedirect(googleLoginUrl);
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String session_state,
            HttpServletResponse response) throws IOException {
        log.info("Received Google OAuth callback with code: {}, state: {}", code, state);

        try {
            // Parse client type from state parameter
            String clientType = "web"; // default
            if (state != null && state.contains(":")) {
                clientType = state.split(":")[0];
            }

            // Exchange authorization code for tokens
            KeycloakCallbackRequest callbackRequest = KeycloakCallbackRequest.builder()
                    .code(code)
                    .redirectUri(authProperties.getGoogleCallbackUri())
                    .build();

            AuthTokenResponse authTokenResponse = keycloakAuthService.exchangeAuthorizationCode(callbackRequest);

            // Decode JWT to get user info
            JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
            Jwt jwt = decoder.decode(authTokenResponse.getAccessToken());

            // Sync user profile if not exists
            userProfileApi.createUserFromKeycloakUser(jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("given_name"),
                    jwt.getClaimAsString("family_name"));

            if ("mobile".equalsIgnoreCase(clientType)) {
                // For mobile, redirect to a deep link or custom scheme
                String mobileRedirectUrl = String.format(
                        "%s/auth/callback?access_token=%s&refresh_token=%s&expires_in=%d",
                        authProperties.getFeUrl().replace("http://", "myapp://"),
                        authTokenResponse.getAccessToken(),
                        authTokenResponse.getRefreshToken(),
                        authTokenResponse.getExpiresIn());

                log.info("Redirecting mobile client to: {}", mobileRedirectUrl);
                response.sendRedirect(mobileRedirectUrl);
            } else {
                // For web, set cookies and redirect to FE URL
                Cookie accessTokenCookie = createCookie("access_token",
                        authTokenResponse.getAccessToken(),
                        authTokenResponse.getExpiresIn());
                Cookie refreshTokenCookie = createCookie("refresh_token", authTokenResponse.getRefreshToken(), MAX_AGE);

                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);

                // Redirect to frontend with success flag
                log.info("Redirecting web client to: {}", authProperties.getFeUrl());
                response.sendRedirect(authProperties.getFeUrl() + "/auth/success");
            }

        } catch (Exception e) {
            log.error("Error during Google OAuth callback: {}", e.getMessage(), e);

            // Parse client type from state for error redirect
            String clientType = "web";
            if (state != null && state.contains(":")) {
                clientType = state.split(":")[0];
            }

            if ("mobile".equalsIgnoreCase(clientType)) {
                response.sendRedirect(authProperties.getFeUrl().replace("http://", "myapp://") + "/auth/error");
            } else {
                response.sendRedirect(authProperties.getFeUrl() + "/auth/error");
            }
        }
    }

    @PostMapping("/google/callback/mobile")
    public ResponseEntity<AppResponseDto<SignInResponse>> googleCallbackMobile(
            @Valid @RequestBody KeycloakCallbackRequest request) {
        log.info("Received Google OAuth callback for mobile");

        try {
            KeycloakCallbackRequest mobileRequest = KeycloakCallbackRequest.builder()
                    .code(request.getCode())
                    .redirectUri(request.getRedirectUri() != null
                            ? request.getRedirectUri()
                            : (authProperties.getGoogleCallbackUri() + "-mobile"))
                    .build();

            // Exchange authorization code for tokens
            AuthTokenResponse authTokenResponse = keycloakAuthService.exchangeAuthorizationCode(mobileRequest);

            // Decode JWT to get user info
            JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
            Jwt jwt = decoder.decode(authTokenResponse.getAccessToken());

            // Sync user profile if not exists
            userProfileApi.createUserFromKeycloakUser(jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("given_name"),
                    jwt.getClaimAsString("family_name"));

            // Map AuthTokenResponse to SignInResponse
            SignInResponse signInResponse = SignInResponse.builder()
                    .accessToken(authTokenResponse.getAccessToken())
                    .refreshToken(authTokenResponse.getRefreshToken())
                    .tokenType(authTokenResponse.getTokenType())
                    .expiresIn(authTokenResponse.getExpiresIn())
                    .build();

            return ResponseEntity.ok(AppResponseDto.success(signInResponse));

        } catch (Exception e) {
            log.error("Error during Google OAuth mobile callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AppResponseDto.<SignInResponse>builder()
                            .success(false)
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Authentication failed: " + e.getMessage())
                            .build());
        }
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        //TODO: when going production, set Secure to true
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");

        log.info("Creating cookie: name={}, maxAge={}, httpOnly=true, sameSite=Lax", name, maxAge);

        return cookie;
    }
}
