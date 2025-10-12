package com.datn.datnbe.auth.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/")
    public String home() {
        return "Hello (public)";
    }

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal OidcUser user) {
        return "Hello " + user.getFullName() + " (" + user.getEmail() + ")";
    }

    @GetMapping("/api/user")
    public String userApi() {
        return "User API";
    }

    @GetMapping("/api/admin/secret")
    public String adminApi() {
        return "Admin API";
    }
}
