package com.datn.datnbe.document.dto.request;

import lombok.Data;

@Data
public class ContextCreateRequest {
    private String title;
    private String content;
    private String subject;
    private Integer grade;
    private String author;
}
