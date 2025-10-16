package com.datn.datnbe.auth.dto.keycloak;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeycloakGroupPolicyDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    @Builder.Default
    private String type = "group";

    @JsonProperty("logic")
    @Builder.Default
    private String logic = "POSITIVE";

    @JsonProperty("decisionStrategy")
    @Builder.Default
    private String decisionStrategy = "UNANIMOUS";

    @JsonProperty("groups")
    private List<GroupDefinition> groups;

    @JsonProperty("groupsClaim")
    private String groupsClaim;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GroupDefinition {
        @JsonProperty("id")
        private String id;

        @JsonProperty("extendChildren")
        @Builder.Default
        private Boolean extendChildren = false;
    }
}
