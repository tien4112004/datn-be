package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.config.properties.AuthProperties;
import com.datn.datnbe.auth.dto.request.SigninRequest;
import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
import com.datn.datnbe.auth.dto.response.SigninResponse;
import com.datn.datnbe.auth.dto.response.UserProfileResponseDto;
import com.datn.datnbe.auth.service.AuthenticationService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthProperties authProperties;
    UserProfileApi userProfileApi;
    AuthenticationService authenticationService;

    @PostMapping("/signin")
    public ResponseEntity<AppResponseDto<SigninResponse>> signIn(@Valid @RequestBody SigninRequest request) {
        SigninResponse response = authenticationService.signIn(request);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<AppResponseDto<UserProfileResponseDto>> signup(
            @Valid @RequestBody UserProfileCreateRequest request) {
        UserProfileResponseDto response = userProfileApi.createUserProfile(request);
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
}
