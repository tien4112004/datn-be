package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.config.properties.AuthProperties;
import com.datn.datnbe.auth.dto.request.UserProfileCreateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponseDto;
import com.datn.datnbe.auth.management.UserProfileManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.datn.datnbe.auth.config.AuthProperties;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthProperties authProperties;
    UserProfileManagement userProfileManagement;

    @GetMapping("/signin")
    public String signin() {
        return "redirect:" + authProperties.getProperty("signin-uri");
    }

    @PostMapping("/signup")
    public ResponseEntity<AppResponseDto<UserProfileResponseDto>> signup(
            @Valid @RequestBody UserProfileCreateRequest request) {
        //        String url = UriComponentsBuilder.fromUriString(authProperties.getProperty("signup-uri"))
        //                .queryParam("client_id", authProperties.getProperty("client-id"))
        //                .queryParam("response_type", "code")
        //                .queryParam("scope", "openid profile email")
        //                .queryParam("redirect_uri", authProperties.getProperty("redirect-uri"))
        //                .queryParam("kc_action", "register")
        //                .toUriString();
        //        return "redirect:" + url;

        return ResponseEntity.ok().body(AppResponseDto.success(userProfileManagement.createUserProfile(request)));
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        new SecurityContextLogoutHandler().logout(request, response, auth);

        String idToken = null;
        if (auth instanceof OAuth2AuthenticationToken oauth && oauth.getPrincipal() instanceof OidcUser oidc) {
            idToken = oidc.getIdToken().getTokenValue();
        }

        String url = UriComponentsBuilder.fromUriString(authProperties.getProperty("logout-uri"))
                .queryParam("post_logout_redirect_uri", authProperties.getProperty("redirect-uri"))
                .queryParam("id_token_hint", idToken)
                .toUriString();

        return "redirect:" + url;
    }
}
