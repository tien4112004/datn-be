package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.SignInResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.service.AuthenticationService;
import com.datn.datnbe.auth.service.OAuthCallbackService;
import com.datn.datnbe.auth.service.SessionManagementService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {
    AuthProperties authProperties;
    UserProfileApi userProfileApi;
    AuthenticationService authenticationService;
    OAuthCallbackService oauthCallbackService;
    SessionManagementService sessionService;

    @PostMapping("/signin")
    public ResponseEntity<AppResponseDto<SignInResponse>> signIn(@Valid @RequestBody SigninRequest request,
            HttpServletResponse response) {
        SignInResponse signInResponse = authenticationService.signIn(request);
        sessionService.createSession(response, signInResponse);

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
        sessionService.clearSession(response);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/google/authorize")
    public void googleAuthorize(@RequestParam(required = false, defaultValue = "web") String clientType,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // Extract and store frontend origin in session
        String feOrigin = sessionService.extractFrontendOrigin(request);
        request.getSession().setAttribute("fe_origin", feOrigin);
        log.info("Stored frontend origin in session: {}", feOrigin);

        String googleLoginUrl = oauthCallbackService.generateGoogleLoginUrl(clientType);

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
            String clientType = oauthCallbackService.extractClientType(state);

            // Exchange authorization code for tokens and sync user
            KeycloakCallbackRequest callbackRequest = KeycloakCallbackRequest.builder()
                    .code(code)
                    .redirectUri(authProperties.getGoogleCallbackUri())
                    .build();

            SignInResponse signInResponse = oauthCallbackService.processCallback(callbackRequest);

            // Retrieve frontend origin from session
            String feUrl = (String) request.getSession().getAttribute("fe_origin");
            if (feUrl == null || feUrl.isEmpty()) {
                feUrl = sessionService.extractFrontendOrigin(request);
            }
            log.info("Retrieved frontend origin from session: {}", feUrl);

            handleSuccessfulCallback(response, signInResponse, clientType, feUrl);

        } catch (Exception e) {
            log.error("Error during Google OAuth callback: {}", e.getMessage(), e);
            handleFailedCallback(response, state, request);
        }
    }

    private void handleSuccessfulCallback(HttpServletResponse response,
            SignInResponse signInResponse,
            String clientType,
            String feUrl) throws IOException {

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
            sessionService.createSession(response, signInResponse);

            // Redirect to frontend with success flag
            log.info("Redirecting web client to: {}", feUrl);
            response.sendRedirect(feUrl + "/auth/google/callback");
        }
    }

    private void handleFailedCallback(HttpServletResponse response, String state, HttpServletRequest request)
            throws IOException {

        // Parse client type from state for error redirect
        String clientType = oauthCallbackService.extractClientType(state);

        // Retrieve frontend origin from session
        String feUrl = (String) request.getSession().getAttribute("fe_origin");
        if (feUrl == null || feUrl.isEmpty()) {
            feUrl = sessionService.extractFrontendOrigin(request);
        }

        if ("mobile".equalsIgnoreCase(clientType)) {
            response.sendRedirect(authProperties.getMobileRedirectUrl() + "/auth/error");
        } else {
            response.sendRedirect(feUrl + "/auth/error");
        }
    }

}
