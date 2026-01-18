package com.datn.datnbe.document.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AssignmentUpdateRequest {
    private String title;
    private String description;
    private Integer duration;
    private String subject;
    private String grade;
    private List<QuestionItemRequest> questions;

}
