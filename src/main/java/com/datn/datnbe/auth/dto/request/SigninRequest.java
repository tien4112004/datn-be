package com.datn.datnbe.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SigninRequest {

    @Email
    String email;

    @NotBlank(message = "Password is required")
    String password;

    String username;
}
