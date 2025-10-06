package com.datn.datnbe.auth.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {
    @Value("${app.keycloak.issuer}")
    private String issuer;

    @Value("${app.keycloak.client-id}")
    private String clientId;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @GetMapping("/signin")
    public String signin() {
        return "redirect:/oauth2/authorization/keycloak";
    }

    @GetMapping("/signup")
    public String signup() {
        String url = issuer + "/protocol/openid-connect/registrations" + "?client_id=" + urlEncode(clientId)
                + "&response_type=code" + "&scope=" + urlEncode("openid profile email") + "&redirect_uri="
                + urlEncode(redirectUri);
        return "redirect:" + url;
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
