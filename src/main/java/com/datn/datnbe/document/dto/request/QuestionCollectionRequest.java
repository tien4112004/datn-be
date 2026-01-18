package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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

    // Multi-select filter fields - accept arrays from frontend
    List<String> grade;

    List<String> chapter;

    List<String> difficulty;

    List<String> subject;

    List<String> type;

    String sortBy;

    String sortDirection;
}
