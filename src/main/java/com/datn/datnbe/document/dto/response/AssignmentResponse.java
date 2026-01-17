package com.datn.datnbe.document.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentResponse {
    String id;
    String title;
    String description;
    Integer duration;
    String ownerId;
    String subject;
    String grade;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
