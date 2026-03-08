package com.datn.datnbe.document.dto.request;

import lombok.Data;

@Data
public class UpdateDocumentRequest {
    private String chapterId;
    private String chapter;
    private String subject;
    private String grade;
}
