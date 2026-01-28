package com.datn.datnbe.document.dto.request;

import lombok.Data;

@Data
public class ContextUpdateRequest {
    private String content;
    private String grade;
    private String author;
}
