package com.datn.datnbe.auth.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * DTO for Keycloak token response when requesting a client credentials token.
 * Used for service-to-service authentication with Keycloak.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakTokenResponse {

    /**
     * The access token
     */
    @JsonProperty("access_token")
    String accessToken;

    /**
     * The token type (usually "Bearer")
     */
    @JsonProperty("token_type")
    String tokenType;

    /**
     * Token expiration time in seconds
     */
    @JsonProperty("expires_in")
    Integer expiresIn;

    /**
     * Refresh token (if applicable)
     */
    @JsonProperty("refresh_token")
    String refreshToken;

    /**
     * Refresh token expiration time in seconds
     */
    @JsonProperty("refresh_expires_in")
    Integer refreshExpiresIn;
}
