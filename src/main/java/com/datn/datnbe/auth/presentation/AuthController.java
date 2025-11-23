package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.SignInResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.service.AuthenticationService;
import com.datn.datnbe.auth.service.KeycloakAuthService;
import com.datn.datnbe.auth.utils.CookieUtils;
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
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

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

        Cookie accessTokenCookie = CookieUtils
                .createCookie("access_token", signInResponse.getAccessToken(), signInResponse.getExpiresIn());
        Cookie refreshTokenCookie = CookieUtils
                .createCookie("refresh_token", signInResponse.getRefreshToken(), MAX_AGE);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(AppResponseDto.success(signInResponse));
    }

    @PostMapping("/signup")
    public ResponseEntity<AppResponseDto<UserProfileResponse>> signup(@Valid @RequestBody SignupRequest request) {
        UserProfileResponse response = userProfileApi.createUserProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponseDto.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<AppResponseDto<Map<String, String>>> logout(HttpServletRequest request,
            HttpServletResponse response,
            Authentication auth) {
        // Invalidate session
        new SecurityContextLogoutHandler().logout(request, response, auth);

        // Extract refresh token from cookies and invalidate Keycloak session
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName()) && cookie.getValue() != null
                        && !cookie.getValue().isEmpty()) {
                    try {
                        authenticationService.signOut(cookie.getValue());
                        log.info("Successfully invalidated Keycloak session");
                    } catch (Exception e) {
                        log.warn("Failed to invalidate Keycloak session: {}", e.getMessage());
                        // Continue with logout even if Keycloak session invalidation fails
                    }
                    break;
                }
            }
        }

        // Clear cookies
        CookieUtils.deleteCookie(response, "access_token");
        CookieUtils.deleteCookie(response, "refresh_token");
        CookieUtils.deleteCookie(response, "JSESSIONID");

        // Build Keycloak logout URL
        String idToken = null;
        if (auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
            idToken = oidc.getIdToken().getTokenValue();
        }

        String keycloakLogoutUrl = UriComponentsBuilder.fromUriString(authProperties.getLogoutUri())
                .queryParam("post_logout_redirect_uri", authProperties.getRedirectUri())
                .queryParam("id_token_hint", idToken)
                .toUriString();

        log.info("User logged out, Keycloak logout URL: {}", keycloakLogoutUrl);

        return ResponseEntity.ok(AppResponseDto.success(Map.of("logoutUrl", keycloakLogoutUrl)));
    }

    @PostMapping("/exchange")
    public ResponseEntity<AppResponseDto<SignInResponse>> keycloakCallback(
            @Valid @RequestBody KeycloakCallbackRequest request,
            HttpServletResponse response) {

        // Process login callback (exchange code, decode JWT, sync user)
        SignInResponse signInResponse = keycloakAuthService.processLoginCallback(request);

        // Add cookies to response
        Cookie accessTokenCookie = CookieUtils
                .createCookie("access_token", signInResponse.getAccessToken(), signInResponse.getExpiresIn());
        Cookie refreshTokenCookie = CookieUtils
                .createCookie("refresh_token", signInResponse.getRefreshToken(), MAX_AGE);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(AppResponseDto.success(signInResponse));
    }

    @GetMapping("/google/authorize")
    public void googleAuthorize(@RequestParam(required = false, defaultValue = "web") String clientType,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // Extract and store frontend origin in session
        String feOrigin = extractFeOrigin(request);
        request.getSession().setAttribute("fe_origin", feOrigin);
        log.info("Stored frontend origin in session: {}", feOrigin);

        String googleLoginUrl = keycloakAuthService.generateGoogleLoginUrl(clientType);

        log.info("Redirecting to Google login with clientType: {}", clientType);
        response.sendRedirect(googleLoginUrl);
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String session_state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        log.info("Received Google OAuth callback with code: {}, state: {}", code, state);

        try {
            // Parse client type from state parameter
            String clientType = "web"; // default
            if (state != null && state.contains(":")) {
                clientType = state.split(":")[0];
            }

            // Exchange authorization code for tokens and sync user
            KeycloakCallbackRequest callbackRequest = KeycloakCallbackRequest.builder()
                    .code(code)
                    .redirectUri(authProperties.getGoogleCallbackUri())
                    .build();

            SignInResponse signInResponse = keycloakAuthService.processLoginCallback(callbackRequest);

            // Retrieve frontend origin from session
            String feUrl = (String) request.getSession().getAttribute("fe_origin");
            if (feUrl == null || feUrl.isEmpty()) {
                feUrl = getOriginUrl(request); // fallback
            }
            log.info("Retrieved frontend origin from session: {}", feUrl);

            if ("mobile".equalsIgnoreCase(clientType)) {
                // For mobile, redirect to a deep link or custom scheme
                String mobileRedirectUrl = String.format("%s?access_token=%s&refresh_token=%s&expires_in=%d",
                        authProperties.getMobileRedirectUrl(),
                        signInResponse.getAccessToken(),
                        signInResponse.getRefreshToken(),
                        signInResponse.getExpiresIn());

                log.info("Redirecting mobile client to: {}", mobileRedirectUrl);
                response.sendRedirect(mobileRedirectUrl);
            } else {
                // For web, set cookies and redirect to FE URL
                Cookie accessTokenCookie = CookieUtils
                        .createCookie("access_token", signInResponse.getAccessToken(), signInResponse.getExpiresIn());
                Cookie refreshTokenCookie = CookieUtils
                        .createCookie("refresh_token", signInResponse.getRefreshToken(), MAX_AGE);

                response.addCookie(accessTokenCookie);
                response.addCookie(refreshTokenCookie);

                // Redirect to frontend with success flag
                log.info("Redirecting web client to: {}", feUrl);
                response.sendRedirect(feUrl + "/auth/google/callback");
            }

        } catch (Exception e) {
            log.error("Error during Google OAuth callback: {}", e.getMessage(), e);

            // Parse client type from state for error redirect
            String clientType = "web";
            if (state != null && state.contains(":")) {
                clientType = state.split(":")[0];
            }

            // Retrieve frontend origin from session
            String feUrl = (String) request.getSession().getAttribute("fe_origin");
            if (feUrl == null || feUrl.isEmpty()) {
                feUrl = getOriginUrl(request); // fallback
            }

            if ("mobile".equalsIgnoreCase(clientType)) {
                response.sendRedirect(authProperties.getMobileRedirectUrl() + "/auth/error");
            } else {
                response.sendRedirect(feUrl + "/auth/error");
            }
        }
    }

    @Deprecated
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

            // Process login callback (exchange code, decode JWT, sync user)
            SignInResponse signInResponse = keycloakAuthService.processLoginCallback(mobileRequest);

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

    private String extractFeOrigin(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            try {
                // Extract origin from referer URL (e.g., "http://localhost:3000/..." -> "http://localhost:3000")
                java.net.URI uri = java.net.URI.create(referer);
                String origin = uri.getScheme() + "://" + uri.getHost();
                if ((uri.getScheme().equals("http") && uri.getPort() != 80 && uri.getPort() != -1)
                        || (uri.getScheme().equals("https") && uri.getPort() != 443 && uri.getPort() != -1)) {
                    origin += ":" + uri.getPort();
                }
                log.debug("Extracted frontend origin from Referer: {}", origin);
                return origin;
            } catch (Exception e) {
                log.warn("Failed to parse Referer header: {}", e.getMessage());
            }
        }
        // Fallback to request origin if Referer is not available
        return getOriginUrl(request);
    }

    private String getOriginUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isEmpty()) {
            scheme = request.getScheme();
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = request.getHeader("Host");
        }

        if (host == null || host.isEmpty()) {
            host = request.getServerName();
            int port = request.getServerPort();
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                host = host + ":" + port;
            }
        }

        log.debug("Extracted origin URL - scheme: {}, host: {}", scheme, host);
        return scheme + "://" + host;
    }
}
