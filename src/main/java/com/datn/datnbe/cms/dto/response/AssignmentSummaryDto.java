package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignmentSummaryDto {

    private String assignmentId;
    private String title;
    private Instant dueDate;
    private Integer totalSubmissions;
    private Integer gradedSubmissions;
    private Double averageScore;
    private Double participationRate;
}
