package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionCollectionRequest {

    @NotNull(message = "Bank type is required (personal or public)")
    String bankType;

    @Builder.Default
    Integer page = 1;

    @Builder.Default
    Integer pageSize = 10;

    String search;

    String sortBy;

    String sortDirection;
}
