package com.datn.datnbe.cms.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchCreateQuestionResponseDto {
    List<QuestionResponseDto> successful;
    List<BatchItemErrorDto> failed;
    Integer totalProcessed;
    Integer totalSuccessful;
    Integer totalFailed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BatchItemErrorDto {
        Integer index;
        String title;
        String errorMessage;
        Map<String, String> fieldErrors;
    }
}
