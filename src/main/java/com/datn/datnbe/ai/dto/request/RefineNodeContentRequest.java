package com.datn.datnbe.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class RefineNodeContentRequest {
    @NotBlank(message = "Node ID cannot be blank")
    String nodeId;

    @NotBlank(message = "Current content cannot be blank")
    @Size(max = 500, message = "Content must not exceed 500 characters")
    String currentContent;

    @NotBlank(message = "Instruction cannot be blank")
    String instruction;

    String operation; // expand, shorten, grammar, formal

    TreeContext context;

    String model;
    String provider;
}
