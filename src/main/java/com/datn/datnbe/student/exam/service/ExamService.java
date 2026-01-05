package com.datn.datnbe.student.exam.service;

import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.student.exam.api.ExamApi;
import com.datn.datnbe.student.exam.apiclient.ExamGenerationApiClient;
import com.datn.datnbe.student.exam.dto.request.CreateExamRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateMatrixRequest;
import com.datn.datnbe.student.exam.dto.request.GenerateQuestionsRequest;
import com.datn.datnbe.student.exam.dto.request.UpdateExamRequest;
import com.datn.datnbe.student.exam.dto.response.ExamDetailDto;
import com.datn.datnbe.student.exam.dto.response.ExamResponseDto;
import com.datn.datnbe.student.exam.dto.response.ExamSummaryDto;
import com.datn.datnbe.student.exam.entity.Exam;
import com.datn.datnbe.student.exam.enums.ExamStatus;
import com.datn.datnbe.student.exam.enums.GradeLevel;
import com.datn.datnbe.student.exam.mapper.ExamEntityMapper;
import com.datn.datnbe.student.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ExamService implements ExamApi {

    ExamRepository examRepository;
    ExamEntityMapper examMapper;
    ExamGenerationApiClient examGenerationApiClient;

    @Override
    @Transactional
    public ExamResponseDto createExam(CreateExamRequest request, UUID teacherId) {
        log.info("Creating exam for teacher: {}", teacherId);

        Exam exam = examMapper.createRequestToEntity(request);
        exam.setTeacherId(teacherId);
        exam.setStatus(ExamStatus.DRAFT);

        Exam savedExam = examRepository.save(exam);
        log.info("Created exam with ID: {}", savedExam.getExamId());

        return examMapper.entityToResponseDto(savedExam);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ExamSummaryDto> getAllExams(UUID teacherId,
            Pageable pageable,
            ExamStatus status,
            String topic,
            GradeLevel gradeLevel) {
        log.info("Fetching exams for teacher: {} with filters - status: {}, topic: {}, gradeLevel: {}",
                teacherId,
                status,
                topic,
                gradeLevel);

        Page<Exam> examPage = examRepository.findByTeacherIdWithFilters(teacherId, status, topic, gradeLevel, pageable);

        Page<ExamSummaryDto> dtoPage = examPage.map(examMapper::entityToSummaryDto);

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalItems(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .build();

        return PaginatedResponseDto.<ExamSummaryDto>builder().data(dtoPage.getContent()).pagination(pagination).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDetailDto getExamById(UUID examId, UUID teacherId) {
        log.info("Fetching exam: {} for teacher: {}", examId, teacherId);

        Exam exam = examRepository.findByExamIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND, "Exam not found with ID: " + examId));

        return examMapper.entityToDetailDto(exam);
    }

    @Override
    @Transactional
    public ExamDetailDto updateExam(UUID examId, UpdateExamRequest request, UUID teacherId) {
        log.info("Updating exam: {} for teacher: {}", examId, teacherId);

        Exam exam = examRepository.findByExamIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND, "Exam not found with ID: " + examId));

        examMapper.updateEntity(request, exam);

        Exam updatedExam = examRepository.save(exam);
        log.info("Updated exam: {}", examId);

        return examMapper.entityToDetailDto(updatedExam);
    }

    @Override
    @Transactional
    public void deleteExam(UUID examId, UUID teacherId) {
        log.info("Deleting exam: {} for teacher: {}", examId, teacherId);

        Exam exam = examRepository.findByExamIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND, "Exam not found with ID: " + examId));

        // TODO: Check if exam has assignments before deletion

        examRepository.delete(exam);
        log.info("Deleted exam: {}", examId);
    }

    @Override
    @Transactional
    public void archiveExam(UUID examId, UUID teacherId) {
        log.info("Archiving exam: {} for teacher: {}", examId, teacherId);

        Exam exam = examRepository.findByExamIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND, "Exam not found with ID: " + examId));

        exam.setStatus(ExamStatus.ARCHIVED);
        examRepository.save(exam);
        log.info("Archived exam: {}", examId);
    }

    @Override
    @Transactional
    public ExamResponseDto duplicateExam(UUID examId, UUID teacherId) {
        log.info("Duplicating exam: {} for teacher: {}", examId, teacherId);

        Exam originalExam = examRepository.findByExamIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND, "Exam not found with ID: " + examId));

        Exam duplicatedExam = Exam.builder()
                .teacherId(teacherId)
                .title(originalExam.getTitle() + " (Copy)")
                .description(originalExam.getDescription())
                .topic(originalExam.getTopic())
                .gradeLevel(originalExam.getGradeLevel())
                .difficulty(originalExam.getDifficulty())
                .totalQuestions(originalExam.getTotalQuestions())
                .totalPoints(originalExam.getTotalPoints())
                .timeLimitMinutes(originalExam.getTimeLimitMinutes())
                .questionOrder(originalExam.getQuestionOrder())
                .status(ExamStatus.DRAFT)
                .build();

        Exam savedExam = examRepository.save(duplicatedExam);
        log.info("Duplicated exam with new ID: {}", savedExam.getExamId());

        return examMapper.entityToResponseDto(savedExam);
    }

    @Override
    public ExamMatrixDto generateMatrix(GenerateMatrixRequest request) {
        log.info("Generating exam matrix for topic: {}", request.getTopic());
        return examGenerationApiClient.generateMatrix(request);
    }

    @Override
    @Transactional
    public String generateQuestions(UUID examId, GenerateQuestionsRequest request, UUID teacherId) {
        log.info("Generating questions for exam: {}", examId);

        Exam exam = examRepository.findByExamIdAndTeacherId(examId, teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND, "Exam not found with ID: " + examId));

        // Update exam status to GENERATING
        exam.setStatus(ExamStatus.GENERATING);
        examRepository.save(exam);

        try {
            String response = examGenerationApiClient.generateQuestions(request);
            
            // Update exam status to COMPLETED after generation
            exam.setStatus(ExamStatus.COMPLETED);
            examRepository.save(exam);
            log.info("Question generation completed for exam: {}", examId);
            
            return response;
        } catch (Exception error) {
            // Update exam status to ERROR on failure
            exam.setStatus(ExamStatus.ERROR);
            examRepository.save(exam);
            log.error("Question generation failed for exam: {}", examId, error);
            throw error;
        }
    }
}
