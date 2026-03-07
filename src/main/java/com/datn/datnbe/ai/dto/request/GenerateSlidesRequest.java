package com.datn.datnbe.ai.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class GenerateSlidesRequest {
    private String prompt;
    private Integer slideCount;
    private String model;
    private String provider;
    private String artStyle;
    private String imageModel;
    private String imageProvider;
    private String negativePrompt;
    private Map<String, Object> context;
    private String language;
    private String presentationId;
    private String grade;
    private String subject;
    private String chapter;
}
