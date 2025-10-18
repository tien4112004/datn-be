package com.datn.datnbe.auth.dto.keycloak;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakResourceDto {

    @JsonProperty("_id")
    String id;

    String name;

    String type;

    Set<String> uris;

    Set<String> scopes;

    String owner;

    @Builder.Default
    @JsonProperty("ownerManagedAccess")
    Boolean ownerManagedAccess = true;

    @JsonProperty("displayName")
    String displayName;

    Object attributes;
}
