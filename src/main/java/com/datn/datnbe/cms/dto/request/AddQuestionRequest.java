package com.datn.datnbe.cms.dto.request;

import lombok.Data;

@Data
public class AddQuestionRequest {
    private String questionId;
    private Double point;
    private Integer order;
}
