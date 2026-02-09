package com.datn.datnbe.document.dto.response;

import lombok.*;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.AssignmentMatrix;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

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
    private String grade;
    private List<com.datn.datnbe.document.entity.AssignmentContext> contexts;
    private List<Question> questions;
    private AssignmentMatrix matrix;
    private LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
