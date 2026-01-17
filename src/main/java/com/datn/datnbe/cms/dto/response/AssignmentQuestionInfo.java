package com.datn.datnbe.cms.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentQuestionInfo {
    String id;
    String assignmentId;
    QuestionResponseDto question;
    Double point;
    Integer order;
}
