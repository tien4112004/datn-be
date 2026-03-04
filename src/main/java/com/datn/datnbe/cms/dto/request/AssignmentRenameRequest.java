package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRenameRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}
