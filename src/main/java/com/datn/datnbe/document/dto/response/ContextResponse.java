package com.datn.datnbe.document.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContextResponse {
    String id;
    String title;
    String grade;
    String author;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
