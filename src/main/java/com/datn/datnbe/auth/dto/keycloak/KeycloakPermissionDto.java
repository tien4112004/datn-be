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
public class KeycloakPermissionDto {

    String id;

    String name;

    String description;

    @Builder.Default
    String type = "scope";

    @Builder.Default
    String logic = "POSITIVE";

    @Builder.Default
    @JsonProperty("decisionStrategy")
    String decisionStrategy = "AFFIRMATIVE";

    Set<String> resources;

    Set<String> scopes;

    Set<String> policies;
}
