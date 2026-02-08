package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.document.entity.AssessmentMatrixCell;
import com.datn.datnbe.document.entity.AssignmentContext;
import com.datn.datnbe.document.entity.AssignmentTopic;
import lombok.*;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.AssignmentMatrix;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignmentResponse {
    String id;
    String title;
    String description;
    Integer duration;
    String ownerId;
    String subject;
    String grade;
    List<AssignmentContext> contexts;
    List<Question> questions;
    AssignmentMatrix matrix;
    LocalDateTime createdAt;

    LocalDateTime updatedAt;
    List<AssignmentTopic> topics;
    List<AssessmentMatrixCell> matrixCells;
    Integer maxSubmissions;
    Boolean allowRetake;
    Boolean shuffleQuestions;
    Boolean showCorrectAnswers;
    Boolean showScoreImmediately;
    Double passingScore;
    LocalDateTime availableFrom;
    LocalDateTime availableUntil;
}
