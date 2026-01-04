package com.datn.datnbe.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRegistrationRequest {

    @NotBlank(message = "document's ID is required")
    private String id;

    @NotBlank(message = "document's name is required")
    private String name;

    @NotBlank(message = "document's type is required")
    private String resourceType;

    private String thumbnail;
}
