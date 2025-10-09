package com.datn.datnbe.auth.presentation;

import com.datn.datnbe.auth.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class AuthController {
    private final AuthProperties authProperties;

    public AuthController(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @GetMapping("/signin")
    public String signin() {
        return "redirect:" + authProperties.getProperty("signin-uri");
    }

    @GetMapping("/signup")
    public String signup() {
        String url = UriComponentsBuilder.fromUriString(authProperties.getProperty("signup-uri"))
                .queryParam("client_id", authProperties.getProperty("client-id"))
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("redirect_uri", authProperties.getProperty("redirect-uri"))
                .queryParam("kc_action", "register")
                .toUriString();
        return "redirect:" + url;
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
