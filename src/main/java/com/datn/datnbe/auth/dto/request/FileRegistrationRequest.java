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
public class FileRegistrationRequest {

    @NotBlank(message = "File ID is required")
    private String fileId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @Builder.Default
    private String resourceType = "files";

    private String ownerId;
}
