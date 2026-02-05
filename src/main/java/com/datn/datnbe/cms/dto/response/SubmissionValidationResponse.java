package com.datn.datnbe.cms.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionValidationResponse {
    Boolean valid;
    List<String> errors;
    List<String> warnings;
}
