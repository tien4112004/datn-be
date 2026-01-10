package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponseDto {

    String id;

    String title;

    String type;

    String difficulty;

    String explanation;

    String titleImageUrl;

    String grade;

    String chapter;

    Object data;

    String ownerId;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
