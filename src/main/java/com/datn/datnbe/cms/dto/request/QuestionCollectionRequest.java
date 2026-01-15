package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionCollectionRequest {

    @NotNull(message = "Bank type is required (personal or public)")
    @Pattern(regexp = "personal|public", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Bank type must be either 'personal' or 'public'")
    String bankType;

    @Builder.Default
    Integer page = 1;

    @Builder.Default
    Integer pageSize = 10;

    String search;

    String grade;

    String chapter;

    String difficulty;

    String subject;

    String type;

    String sortBy;

    String sortDirection;
}
