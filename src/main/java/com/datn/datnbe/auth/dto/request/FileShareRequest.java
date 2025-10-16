package com.datn.datnbe.auth.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class FileShareRequest {

    @NotBlank(message = "Target user ID is required")
    String targetUserId;

    @NotNull(message = "Permissions are required")
    @NotEmpty(message = "At least one permission must be specified")
    Set<String> permissions;
}
