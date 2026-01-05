package com.datn.datnbe.student.exam.mapper;

import com.datn.datnbe.student.exam.dto.request.CreateExamRequest;
import com.datn.datnbe.student.exam.dto.request.UpdateExamRequest;
import com.datn.datnbe.student.exam.dto.response.ExamDetailDto;
import com.datn.datnbe.student.exam.dto.response.ExamResponseDto;
import com.datn.datnbe.student.exam.dto.response.ExamSummaryDto;
import com.datn.datnbe.student.exam.entity.Exam;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
@Named("ExamEntityMapper")
public interface ExamEntityMapper {

    @Mapping(target = "examId", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    @Mapping(target = "totalQuestions", constant = "0")
    @Mapping(target = "totalPoints", constant = "0")
    @Mapping(target = "questionOrder", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Exam createRequestToEntity(CreateExamRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "examId", ignore = true)
    @Mapping(target = "teacherId", ignore = true)
    @Mapping(target = "topic", ignore = true)
    @Mapping(target = "gradeLevel", ignore = true)
    @Mapping(target = "difficulty", ignore = true)
    @Mapping(target = "totalQuestions", ignore = true)
    @Mapping(target = "totalPoints", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateExamRequest request, @MappingTarget Exam exam);

    ExamSummaryDto entityToSummaryDto(Exam exam);

    ExamDetailDto entityToDetailDto(Exam exam);

    ExamResponseDto entityToResponseDto(Exam exam);
}
