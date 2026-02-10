package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.cms.api.PostApi;
import com.datn.datnbe.cms.api.SubmissionApi;
import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.request.SubmissionGradeRequest;
import com.datn.datnbe.cms.dto.request.SubmissionValidationRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.dto.response.SubmissionStatisticsDto;
import com.datn.datnbe.cms.dto.response.SubmissionValidationResponse;
import com.datn.datnbe.cms.entity.AssignmentPost;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.entity.answerData.AnswerData;
import com.datn.datnbe.cms.entity.answerData.FillInBlankAnswer;
import com.datn.datnbe.cms.entity.answerData.MatchingAnswer;
import com.datn.datnbe.cms.entity.answerData.MultipleChoiceAnswer;
import com.datn.datnbe.cms.mapper.SubmissionMapper;
import com.datn.datnbe.cms.repository.AssignmentPostRepository;
import com.datn.datnbe.cms.repository.SubmissionRepository;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.document.entity.questiondata.BlankSegment;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import com.datn.datnbe.document.entity.questiondata.MatchingData;
import com.datn.datnbe.document.entity.questiondata.MatchingPair;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceData;
import com.datn.datnbe.document.entity.questiondata.MultipleChoiceOption;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService implements SubmissionApi {

    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final SecurityContextUtils securityContextUtils;
    private final PostApi postApi;
    private final UserProfileApi userProfileApi;
    private final AssignmentPostRepository assignmentPostRepository;

    @Override
    public synchronized SubmissionResponseDto createSubmission(String postId, SubmissionCreateRequest request) {
        Submission submission = submissionMapper.toEntity(request, postId);

        // Set student ID from authenticated user
        String currentUserId = securityContextUtils.getCurrentUserProfileId();
        submission.setStudentId(currentUserId);

        // Fetch post to extract assignment ID
        PostResponseDto post = postApi.getPostById(postId);

        if (post.getAssignmentId() != null) {
            submission.setAssignmentId(post.getAssignmentId());

            // Fetch assignment from assignment_post table (bypasses permission check)
            try {
                AssignmentPost assignment = assignmentPostRepository.findAssignmentById(post.getAssignmentId());
                if (assignment != null && assignment.getQuestions() != null) {
                    int maxScore = assignment.getQuestions()
                            .stream()
                            .mapToInt(q -> q.getPoint() != null ? q.getPoint().intValue() : 0)
                            .sum();
                    submission.setMaxScore(maxScore);
                }
            } catch (Exception e) {
                log.warn("Could not fetch assignment to calculate max score", e);
            }
        }

        // Set submitted timestamp
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus("pending");

        Submission saved = submissionRepository.save(submission);

        gradeSubmission(saved);

        return enrichSubmissionDto(submissionMapper.toDto(saved));
    }

    @Override
    public List<SubmissionResponseDto> getSubmissions(String postId) {
        List<Submission> list = submissionRepository.findByPostId(postId);
        return enrichSubmissionDtoList(list.stream().map(submissionMapper::toDto).collect(Collectors.toList()));
    }

    @Override
    public SubmissionResponseDto getSubmissionById(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        String currentUser = securityContextUtils.getCurrentUserProfileId();
        String currentKeycloakUser = securityContextUtils.getCurrentUserProfileId();

        PostResponseDto post = postApi.getPostById(s.getPostId());

        // Fallback for older submissions that only have studentId as keycloakId
        if (currentUser.equals(post.getAuthorId()) || currentUser.equals(s.getStudentId())
                || currentKeycloakUser.equals(s.getStudentId())) {
            return enrichSubmissionDto(submissionMapper.toDto(s));
        }

        throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found");
    }

    @Override
    public void deleteSubmission(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));
        submissionRepository.delete(s);
    }

    @Override
    public SubmissionResponseDto gradeSubmissionManually(String submissionId, SubmissionGradeRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        // Verify teacher is the author of the assignment
        PostResponseDto post = postApi.getPostById(submission.getPostId());
        String currentUser = securityContextUtils.getCurrentUserProfileId();
        String currentKeycloakUser = securityContextUtils.getCurrentUserProfileId();

        if (!currentUser.equals(post.getAuthorId()) || !currentKeycloakUser.equals(post.getAuthorId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "You don't have permission to grade this submission");
        }

        // Update scores based on request
        Map<String, Integer> questionScores = request.getQuestionScores();
        if (questionScores != null && !questionScores.isEmpty()) {
            double totalScore = questionScores.values().stream().mapToInt(Integer::intValue).sum();

            submission.setScore(totalScore);

            Map<String, String> questionFeedback = request.getQuestionFeedback();
            List<AnswerData> answers = submission.getQuestions();

            for (Map.Entry<String, Integer> entry : questionScores.entrySet()) {
                String questionId = entry.getKey();
                String feedback = (questionFeedback != null) ? questionFeedback.get(questionId) : null;
                answers.stream().filter(a -> a.getId().equals(questionId)).findFirst().ifPresent(a -> {
                    a.setPoint(entry.getValue().doubleValue());
                    if (feedback != null) {
                        a.setFeedback(feedback);
                    }
                    a.setAutoGraded(false);
                });
            }

            // Set overall feedback
            if (request.getOverallFeedback() != null && !request.getOverallFeedback().trim().isEmpty()) {
                submission.setFeedback(request.getOverallFeedback());
            }

            // Set grading metadata
            submission.setGradedBy(currentUser);
            submission.setGradedAt(LocalDateTime.now());
            submission.setStatus("graded");

            submissionRepository.save(submission);
        }

        return enrichSubmissionDto(submissionMapper.toDto(submission));
    }

    @Async
    protected void gradeSubmission(Submission submission) {
        try {
            log.info("Grading submission: {}", submission.getId());
            var assignment = postApi.getAssignmentByPostId(submission.getPostId());
            List<Question> questions = assignment.getQuestions();
            List<AnswerData> answers = submission.getQuestions();

            Map<String, AnswerData> answerMap = answers.stream().collect(Collectors.toMap(AnswerData::getId, a -> a));

            // Create detailed grading list
            double totalScore = 0;

            for (Question question : questions) {
                double points = gradeQuestion(question, answerMap);
                totalScore += points;
            }

            submission.setStatus("graded");
            submission.setScore(totalScore);

            submissionRepository.save(submission);
            log.info("Grading completed for submission: {}. Score: {}", submission.getId(), totalScore);

        } catch (Exception e) {
            log.error("Grading failed for submission: {}", submission.getId(), e);
        }
    }

    private double gradeQuestion(Question question, Map<String, AnswerData> answerMap) {
        AnswerData answer = answerMap.get(question.getId());
        if (answer == null) {
            log.warn("Answer not found for question: {}", question.getId());
            return 0;
        }
        answer.setAutoGraded(true);

        return switch (question.getType()) {
            case MULTIPLE_CHOICE : {
                double point = gradeMultipleChoice(question, answer);
                log.info("Question {} (MULTIPLE_CHOICE) - Points: {}", question.getId(), point);
                answer.setPoint(point);
                yield point;
            }
            case FILL_IN_BLANK : {
                double point = gradeFillInBlank(question, answer);
                log.info("Question {} (FILL_IN_BLANK) - Points: {}", question.getId(), point);
                answer.setPoint(point);
                yield point;
            }
            case MATCHING : {
                double point = gradeMatching(question, answer);
                log.info("Question {} (MATCHING) - Points: {}", question.getId(), point);
                answer.setPoint(point);
                yield point;
            }
            default :
                yield 0;
        };
    }

    private double gradeMultipleChoice(Question question, AnswerData answer) {
        MultipleChoiceData mcData = (MultipleChoiceData) question.getData();
        MultipleChoiceOption correctOption = mcData.getOptions()
                .stream()
                .filter(MultipleChoiceOption::getIsCorrect)
                .findFirst()
                .orElse(null);

        if (correctOption == null) {
            log.info("  No correct option found for question: {}", question.getId());
            return 0;
        }

        MultipleChoiceAnswer mcAnswer = (MultipleChoiceAnswer) answer.getAnswer();
        double points = mcAnswer != null && mcAnswer.verifyAnswer(correctOption.getId())
                ? question.getPoint().doubleValue()
                : 0;
        log.info("  Student answer: {} | Correct answer: {} | Correct: {} | Total points: {}",
                mcAnswer != null ? mcAnswer.getId() : "null",
                correctOption.getId(),
                mcAnswer != null && mcAnswer.verifyAnswer(correctOption.getId()),
                points);
        return points;
    }

    private double gradeFillInBlank(Question question, AnswerData answer) {
        FillInBlankData fillInBlankData = (FillInBlankData) question.getData();
        List<BlankSegment> blankSegments = fillInBlankData.getSegments()
                .stream()
                .filter(s -> s.getType() == BlankSegment.SegmentType.BLANK)
                .toList();

        if (blankSegments.isEmpty()) {
            log.info("  No blank segments found for question: {}", question.getId());
            return 0;
        }

        FillInBlankAnswer fillAnswer = (FillInBlankAnswer) answer.getAnswer();
        if (fillAnswer == null || fillAnswer.getBlankAnswers() == null) {
            log.info("  No answer provided for question: {}", question.getId());
            return 0;
        }

        double totalPoint = question.getPoint().doubleValue();
        double pointPerBlank = totalPoint / blankSegments.size();

        log.info("  Total points: {} | Points per blank: {} | Total blanks: {}",
                totalPoint,
                pointPerBlank,
                blankSegments.size());

        double totalScore = blankSegments.stream().mapToDouble(segment -> {
            String studentAnswer = fillAnswer.getBlankAnswers().get(segment.getId());
            boolean isCorrect = studentAnswer != null && segment.getAcceptableAnswers().contains(studentAnswer);
            double points = isCorrect ? pointPerBlank : 0;

            log.info("    Segment: {} | Student: '{}' | Acceptable: {} | Correct: {} | Points: {}",
                    segment.getId(),
                    studentAnswer,
                    segment.getAcceptableAnswers(),
                    isCorrect,
                    points);

            return points;
        }).sum();

        log.info("  Fill-In-Blank total score: {}", totalScore);
        return totalScore;
    }

    private double gradeMatching(Question question, AnswerData answer) {
        MatchingData matchingData = (MatchingData) question.getData();
        List<MatchingPair> pairs = matchingData.getPairs();

        if (pairs == null || pairs.isEmpty()) {
            log.info("  No matching pairs found for question: {}", question.getId());
            return 0;
        }

        MatchingAnswer matchAnswer = (MatchingAnswer) answer.getAnswer();
        if (matchAnswer == null) {
            log.info("  No answer provided for question: {}", question.getId());
            return 0;
        }

        Map<String, String> studentPairs = matchAnswer.getMatchedPairs();
        if (studentPairs == null || studentPairs.isEmpty()) {
            log.info("  Student matched pairs is empty for question: {}", question.getId());
            return 0;
        }

        double totalPoint = question.getPoint().doubleValue();
        double pointPerPair = totalPoint / pairs.size();

        log.info("  Total points: {} | Points per pair: {} | Total pairs: {}", totalPoint, pointPerPair, pairs.size());

        double totalScore = pairs.stream().mapToDouble(pair -> {
            String leftKey = pair.getLeft() != null && !pair.getLeft().isBlank()
                    ? pair.getLeft()
                    : pair.getLeftImageUrl();
            String rightValue = pair.getRight() != null && !pair.getRight().isBlank()
                    ? pair.getRight()
                    : pair.getRightImageUrl();

            String studentValue = studentPairs.get(leftKey);
            boolean isCorrect = studentValue != null && studentValue.equals(rightValue);
            double points = isCorrect ? pointPerPair : 0;

            log.info("    Left: {} | Expected: {} | Student: {} | Correct: {} | Points: {}",
                    leftKey,
                    rightValue,
                    studentValue,
                    isCorrect,
                    points);

            return points;
        }).sum();

        log.info("  Matching total score: {}", totalScore);
        return totalScore;
    }

    // New methods for assignment feature alignment

    @Override
    public List<SubmissionResponseDto> getSubmissionsByAssignmentId(String assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return enrichSubmissionDtoList(submissions.stream().map(submissionMapper::toDto).collect(Collectors.toList()));
    }

    @Override
    public List<SubmissionResponseDto> getSubmissionsByAssignmentIdAndStudentId(String assignmentId, String studentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        return enrichSubmissionDtoList(submissions.stream().map(submissionMapper::toDto).collect(Collectors.toList()));
    }

    @Override
    public SubmissionStatisticsDto getSubmissionStatistics(String postId) {
        List<Submission> submissions = submissionRepository.findByPostId(postId);
        return calculateStatistics(submissions);
    }

    @Override
    public SubmissionStatisticsDto getAssignmentStatistics(String assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return calculateStatistics(submissions);
    }

    @Override
    public SubmissionValidationResponse validateSubmission(String assignmentId, SubmissionValidationRequest request) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // Fetch assignment from assignment_post table (bypasses permission check)
            AssignmentPost assignment = assignmentPostRepository.findAssignmentById(assignmentId);

            if (assignment == null) {
                errors.add("Assignment not found");
                return SubmissionValidationResponse.builder().valid(false).errors(errors).warnings(warnings).build();
            }

            // Check max submissions limit
            if (assignment.getMaxSubmissions() != null && assignment.getMaxSubmissions() > 0) {
                long submissionCount = submissionRepository.countByAssignmentIdAndStudentId(assignmentId,
                        request.getStudentId());
                if (submissionCount >= assignment.getMaxSubmissions()) {
                    errors.add("Maximum submission limit reached (" + assignment.getMaxSubmissions() + ")");
                }
            }

            // Check availability dates
            LocalDateTime now = LocalDateTime.now();
            if (assignment.getAvailableFrom() != null && now.isBefore(assignment.getAvailableFrom())) {
                errors.add("Assignment is not yet available. Available from: " + assignment.getAvailableFrom());
            }
            if (assignment.getAvailableUntil() != null && now.isAfter(assignment.getAvailableUntil())) {
                errors.add("Assignment submission deadline has passed: " + assignment.getAvailableUntil());
            }

            // Check if all required questions are answered
            if (assignment.getQuestions() != null && request.getAnswers() != null) {
                Map<String, Boolean> answeredMap = request.getAnswers()
                        .stream()
                        .collect(Collectors.toMap(a -> a.getId(), a -> a.getAnswer() != null, (a, b) -> a || b));

                for (Question q : assignment.getQuestions()) {
                    if (!answeredMap.getOrDefault(q.getId(), false)) {
                        warnings.add(
                                "Question '" + (q.getTitle() != null ? q.getTitle() : q.getId()) + "' is not answered");
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error validating submission", e);
            errors.add("Validation error: " + e.getMessage());
        }

        return SubmissionValidationResponse.builder().valid(errors.isEmpty()).errors(errors).warnings(warnings).build();
    }

    // Helper methods

    private SubmissionResponseDto enrichSubmissionDto(SubmissionResponseDto dto) {
        if (dto == null) {
            return null;
        }

        if (dto.getStudentId() != null) {
            try {
                UserMinimalInfoDto student = userProfileApi.getUserMinimalInfo(dto.getStudentId());
                dto.setStudent(student);
            } catch (Exception e) {
                log.warn("Could not fetch student info for {}", dto.getStudentId(), e);
            }
        }

        if (dto.getGradedByUser() == null && dto.getGradedAt() != null) {
            // Try to get grader info from submission entity
            submissionRepository.findById(dto.getId()).ifPresent(submission -> {
                if (submission.getGradedBy() != null) {
                    try {
                        UserMinimalInfoDto grader = userProfileApi.getUserMinimalInfo(submission.getGradedBy());
                        dto.setGradedByUser(grader);
                    } catch (Exception e) {
                        log.warn("Could not fetch grader info for {}", submission.getGradedBy(), e);
                    }
                }
            });
        }

        return dto;
    }

    private List<SubmissionResponseDto> enrichSubmissionDtoList(List<SubmissionResponseDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return dtoList;
        }

        // Batch fetch user info for better performance
        Map<String, UserMinimalInfoDto> userCache = new HashMap<>();

        for (SubmissionResponseDto dto : dtoList) {
            if (dto.getStudentId() != null && !userCache.containsKey(dto.getStudentId())) {
                try {
                    UserMinimalInfoDto student = userProfileApi.getUserMinimalInfo(dto.getStudentId());
                    if (student != null) {
                        userCache.put(dto.getStudentId(), student);
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch student info for {}", dto.getStudentId(), e);
                }
            }
        }

        // Also fetch grader info from database
        List<String> submissionIds = dtoList.stream().map(SubmissionResponseDto::getId).collect(Collectors.toList());
        List<Submission> submissions = submissionRepository.findAllById(submissionIds);
        Map<String, String> graderMap = submissions.stream()
                .filter(s -> s.getGradedBy() != null)
                .collect(Collectors.toMap(Submission::getId, Submission::getGradedBy, (a, b) -> a));

        for (String graderId : graderMap.values()) {
            if (!userCache.containsKey(graderId)) {
                try {
                    UserMinimalInfoDto grader = userProfileApi.getUserMinimalInfo(graderId);
                    if (grader != null) {
                        userCache.put(graderId, grader);
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch grader info for {}", graderId, e);
                }
            }
        }

        // Apply enrichment
        for (SubmissionResponseDto dto : dtoList) {
            if (dto.getStudentId() != null) {
                dto.setStudent(userCache.get(dto.getStudentId()));
            }
            String graderId = graderMap.get(dto.getId());
            if (graderId != null) {
                dto.setGradedByUser(userCache.get(graderId));
            }
        }

        return dtoList;
    }

    private SubmissionStatisticsDto calculateStatistics(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return SubmissionStatisticsDto.builder()
                    .totalSubmissions(0L)
                    .gradedCount(0L)
                    .pendingCount(0L)
                    .inProgressCount(0L)
                    .averageScore(0.0)
                    .scoreDistribution(new HashMap<>())
                    .highestScore(0.0)
                    .lowestScore(0.0)
                    .build();
        }

        long totalSubmissions = submissions.size();
        long gradedCount = submissions.stream().filter(s -> "graded".equals(s.getStatus())).count();
        long pendingCount = submissions.stream().filter(s -> "pending".equals(s.getStatus())).count();
        long inProgressCount = submissions.stream().filter(s -> "in_progress".equals(s.getStatus())).count();

        List<Submission> gradedSubmissions = submissions.stream()
                .filter(s -> "graded".equals(s.getStatus()) && s.getScore() != null)
                .collect(Collectors.toList());

        Double averageScore = gradedSubmissions.isEmpty()
                ? 0.0
                : gradedSubmissions.stream().mapToDouble(Submission::getScore).average().orElse(0.0);

        Double highestScore = gradedSubmissions.isEmpty()
                ? 0.0
                : gradedSubmissions.stream().mapToDouble(Submission::getScore).max().orElse(0.0);
        Double lowestScore = gradedSubmissions.isEmpty()
                ? 0.0
                : gradedSubmissions.stream().mapToDouble(Submission::getScore).min().orElse(0.0);

        // Calculate score distribution
        Map<String, Long> scoreDistribution = new HashMap<>();
        scoreDistribution.put("90-100", gradedSubmissions.stream().filter(s -> s.getScore() >= 90).count());
        scoreDistribution.put("80-89",
                gradedSubmissions.stream().filter(s -> s.getScore() >= 80 && s.getScore() < 90).count());
        scoreDistribution.put("70-79",
                gradedSubmissions.stream().filter(s -> s.getScore() >= 70 && s.getScore() < 80).count());
        scoreDistribution.put("below-70", gradedSubmissions.stream().filter(s -> s.getScore() < 70).count());

        return SubmissionStatisticsDto.builder()
                .totalSubmissions(totalSubmissions)
                .gradedCount(gradedCount)
                .pendingCount(pendingCount)
                .inProgressCount(inProgressCount)
                .averageScore(averageScore)
                .scoreDistribution(scoreDistribution)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .build();
    }
}
