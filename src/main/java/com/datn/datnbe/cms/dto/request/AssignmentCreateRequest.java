package com.datn.datnbe.cms.dto.request;

import lombok.Data;

@Data
public class AssignmentCreateRequest {
    private String title;
    private String description;
    private Integer duration;
    private String subject;
    private String grade;
}
