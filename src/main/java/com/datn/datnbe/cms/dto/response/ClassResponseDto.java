package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassResponseDto {

    String id;
    String name;
    Integer grade;
    String academicYear;
    Integer currentEnrollment;
    String teacherId;
    String classroom;
    String description;
    Boolean isActive;
    SeatingLayoutResponseDto layout;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
