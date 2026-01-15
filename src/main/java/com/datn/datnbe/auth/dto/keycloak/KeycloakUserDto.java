package com.datn.datnbe.auth.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUserDto {

    String id;

    String username;

    String email;

    String firstName;

    String lastName;

    Boolean enabled;
}
