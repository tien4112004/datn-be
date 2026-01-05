package com.datn.datnbe.student.exam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamMatrixDto {
    private String id;
    private String name;
    private String description;
    
    @JsonProperty("subjectCode")
    @JsonAlias("subject_code")
    private String subjectCode;
    
    @JsonProperty("targetTotalPoints")
    @JsonAlias("target_total_points")
    private Integer targetTotalPoints;
    
    private List<TopicDto> topics;
    private List<MatrixContentDto> contents;
    
    @JsonProperty("createdAt")
    @JsonAlias("created_at")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    @JsonAlias("updated_at")
    private String updatedAt;
    
    @JsonProperty("createdBy")
    @JsonAlias("created_by")
    private String createdBy;
}
