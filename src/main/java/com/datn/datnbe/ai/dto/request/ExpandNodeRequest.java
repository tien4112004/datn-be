package com.datn.datnbe.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class ExpandNodeRequest {
    @NotBlank(message = "Node ID cannot be blank")
    String nodeId;

    @NotBlank(message = "Node content cannot be blank")
    @Size(max = 500, message = "Content must not exceed 500 characters")
    String nodeContent;

    @Positive(message = "Max children must be greater than 0")
    int maxChildren = 5;

    @Positive(message = "Max depth must be greater than 0")
    int maxDepth = 2;

    String language = "en";
    String grade;
    String subject;
    String model;
    String provider;
}
