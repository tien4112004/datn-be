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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import static com.datn.datnbe.auth.utils.OriginUtils.extractFeOrigin;
import static com.datn.datnbe.auth.utils.OriginUtils.getOriginUrl;

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

    @PostMapping("/signin")
    public ResponseEntity<AppResponseDto<SignInResponse>> signIn(@Valid @RequestBody SigninRequest request,
            HttpServletResponse response) {
        SignInResponse signInResponse = authenticationService.signIn(request);

        Cookie accessTokenCookie = CookieUtils
                .createCookie("access_token", signInResponse.getAccessToken(), signInResponse.getExpiresIn());
        Cookie refreshTokenCookie = CookieUtils.createCookie(CookieUtils.REFRESH_TOKEN,
                signInResponse.getRefreshToken(),
                CookieUtils.REFRESH_TOKEN_MAX_AGE);

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
        authenticationService.logout(request, response, auth);
        return ResponseEntity.noContent().build();
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
                Cookie refreshTokenCookie = CookieUtils.createCookie(CookieUtils.REFRESH_TOKEN,
                        signInResponse.getRefreshToken(),
                        CookieUtils.REFRESH_TOKEN_MAX_AGE);

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

}
