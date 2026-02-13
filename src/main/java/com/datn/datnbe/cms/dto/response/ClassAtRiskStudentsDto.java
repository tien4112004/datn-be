package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassAtRiskStudentsDto {

    private String classId;
    private String className;
    private Integer totalStudents;
    private Integer atRiskCount;
    private List<AtRiskStudentDto> atRiskStudents;
}
