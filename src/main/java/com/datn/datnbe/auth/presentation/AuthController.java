package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.service.KeycloakAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.AuthProperties;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.response.AuthTokenResponse;
import com.datn.datnbe.auth.dto.response.SignInResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.service.AuthenticationService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.auth.dto.request.KeycloakCallbackRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthProperties authProperties;
    UserProfileApi userProfileApi;
    AuthenticationService authenticationService;
    KeycloakAuthService keycloakAuthService;

    @PostMapping("/signin")
    public ResponseEntity<AppResponseDto<SignInResponse>> signIn(@Valid @RequestBody SigninRequest request) {
        SignInResponse response = authenticationService.signIn(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
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

    @PostMapping("/keycloak/callback")
    public ResponseEntity<AppResponseDto<SignInResponse>> keycloakCallback(
            @Valid @RequestBody KeycloakCallbackRequest request) {

        // Exchange authorization code for tokens using existing KeycloakAuthService
        AuthTokenResponse authTokenResponse = keycloakAuthService.exchangeAuthorizationCode(request);

        JwtDecoder decoder = JwtDecoders.fromIssuerLocation(authProperties.getIssuer());
        Jwt jwt = decoder.decode(authTokenResponse.getAccessToken());

        // sync user profile if not exists
        userProfileApi.createUserFromKeycloakUser(jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"));

        // Map AuthTokenResponse to SignInResponse
        SignInResponse response = SignInResponse.builder()
                .accessToken(authTokenResponse.getAccessToken())
                .refreshToken(authTokenResponse.getRefreshToken())
                .tokenType(authTokenResponse.getTokenType())
                .expiresIn(authTokenResponse.getExpiresIn())
                .build();

        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
