package com.datn.datnbe.document.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublishRequestResponseDto {

    String id;

    String questionId;

    String requesterId;

    String status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    QuestionResponseDto question;
}
