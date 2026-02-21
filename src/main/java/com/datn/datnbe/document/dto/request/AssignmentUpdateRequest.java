package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.entity.AssignmentMatrix;
import lombok.Data;
import java.util.List;

@Data
public class AssignmentUpdateRequest {
    private String title;
    private String description;
    private String subject;
    private String grade;
    private List<QuestionItemRequest> questions;
    private List<com.datn.datnbe.document.entity.AssignmentContext> contexts;
    private AssignmentMatrix matrix;
}
