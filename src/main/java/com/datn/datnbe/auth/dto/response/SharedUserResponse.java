package com.datn.datnbe.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedUserResponse {

    String userId;

    String email;

    String firstName;

    String lastName;

    String avatarUrl;

    String permission; // "read" or "comment"
}
