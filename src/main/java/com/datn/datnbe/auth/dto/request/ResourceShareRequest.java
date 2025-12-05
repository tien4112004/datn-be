package com.datn.datnbe.auth.dto.request;

import java.util.List;

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
public class ResourceShareRequest {

    @NotNull(message = "Target user IDs are required")
    @NotEmpty(message = "At least one target user must be specified")
    List<String> targetUserIds;

    @NotBlank(message = "Permission is required")
    String permission;
}
