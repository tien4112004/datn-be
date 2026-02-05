package com.datn.datnbe.cms.dto.request;

import com.datn.datnbe.cms.dto.AnswerDataDto;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionValidationRequest {
    String studentId;
    List<AnswerDataDto> answers;
}
