package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.entity.AssignmentMatrix;
import lombok.Data;
import java.util.List;

@Data
public class AssignmentCreateRequest {
    private String title;
    private String description;
    private Integer duration;
    private String subject;
    private String grade;
    private List<QuestionItemRequest> questions;
    private AssignmentMatrix matrix;
}
