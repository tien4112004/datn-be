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

/**
 * DTO for creating/updating a user-based policy in Keycloak.
 * User policies define which specific users can access resources.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KeycloakUserPolicyDto {

    /**
     * The unique identifier of the policy (returned by Keycloak after creation)
     */
    String id;

    /**
     * The name of the policy (e.g., "user-{userId}-policy")
     */
    String name;

    /**
     * Description of the policy
     */
    String description;

    /**
     * The type of policy (must be "user")
     */
    @Builder.Default
    String type = "user";

    /**
     * The logic of the policy (POSITIVE or NEGATIVE)
     */
    @Builder.Default
    String logic = "POSITIVE";

    /**
     * The decision strategy (UNANIMOUS, AFFIRMATIVE, or CONSENSUS)
     */
    @Builder.Default
    @JsonProperty("decisionStrategy")
    String decisionStrategy = "UNANIMOUS";

    /**
     * Set of user IDs that this policy applies to
     */
    Set<String> users;
}
