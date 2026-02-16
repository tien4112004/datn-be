package com.datn.datnbe.ai.dto.request;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class RefineBranchRequest {
    @NotEmpty(message = "At least one node is required")
    List<NodeContent> nodes;

    @NotBlank(message = "Instruction cannot be blank")
    String instruction;

    String operation; // expand, shorten, grammar, formal

    TreeContext context;

    String model;
    String provider;
}
