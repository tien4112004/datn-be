package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.api.PostApi;
import com.datn.datnbe.cms.api.SubmissionApi;
import com.datn.datnbe.cms.dto.request.SubmissionCreateRequest;
import com.datn.datnbe.cms.dto.request.SubmissionGradeRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.cms.dto.response.SubmissionResponseDto;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.entity.answerData.AnswerData;
import com.datn.datnbe.cms.entity.answerData.FillInBlankAnswer;
import com.datn.datnbe.cms.entity.answerData.MatchingAnswer;
import com.datn.datnbe.cms.entity.answerData.MultipleChoiceAnswer;
import com.datn.datnbe.cms.mapper.SubmissionMapper;
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

    @Override
    public synchronized SubmissionResponseDto createSubmission(String postId, SubmissionCreateRequest request) {
        Submission submission = submissionMapper.toEntity(request, postId);
        Submission saved = submissionRepository.save(submission);

        gradeSubmission(saved);

        return submissionMapper.toDto(saved);
    }

    @Override
    public List<SubmissionResponseDto> getSubmissions(String postId) {
        List<Submission> list = submissionRepository.findByPostId(postId);
        return list.stream().map(submissionMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public SubmissionResponseDto getSubmissionById(String id) {
        Submission s = submissionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Submission not found"));

        String currentUser = securityContextUtils.getCurrentUserId();

        PostResponseDto post = postApi.getPostById(s.getPostId());

        if (currentUser.equals(post.getAuthorId()) || currentUser.equals(s.getStudentId())) {
            return submissionMapper.toDto(s);
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
        String currentUser = securityContextUtils.getCurrentUserId();

        if (!currentUser.equals(post.getAuthorId())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "You don't have permission to grade this submission");
        }

        // Update scores based on request
        Map<String, Integer> questionScores = request.getQuestionScores();
        if (questionScores != null && !questionScores.isEmpty()) {
            int totalScore = questionScores.values().stream().mapToInt(Integer::intValue).sum();
            submission.setPoint(totalScore);
            submissionRepository.save(submission);
        }

        return submissionMapper.toDto(submission);
    }

    @Async
    private void gradeSubmission(Submission submission) {
        try {
            log.info("Grading submission: {}", submission.getId());
            var assignment = postApi.getAssignmentByPostId(submission.getPostId());
            List<Question> questions = assignment.getQuestions();
            List<AnswerData> answers = submission.getQuestions();

            Map<String, AnswerData> answerMap = answers.stream().collect(Collectors.toMap(AnswerData::getId, a -> a));

            int totalScore = questions.stream().mapToInt(question -> gradeQuestion(question, answerMap)).sum();

            submission.setPoint(totalScore);
            submissionRepository.save(submission);
            log.info("Grading completed for submission: {}. Score: {}", submission.getId(), totalScore);

        } catch (Exception e) {
            log.error("Grading failed for submission: {}", submission.getId(), e);
        }
    }

    private int gradeQuestion(Question question, Map<String, AnswerData> answerMap) {
        AnswerData answer = answerMap.get(question.getId());
        if (answer == null) {
            log.warn("Answer not found for question: {}", question.getId());
            return 0;
        }

        return switch (question.getType()) {
            case MULTIPLE_CHOICE -> gradeMultipleChoice(question, answer);
            case FILL_IN_BLANK -> gradeFillInBlank(question, answer);
            case MATCHING -> gradeMatching(question, answer);
            default -> 0;
        };
    }

    private int gradeMultipleChoice(Question question, AnswerData answer) {
        MultipleChoiceData mcData = (MultipleChoiceData) question.getData();
        MultipleChoiceOption correctOption = mcData.getOptions()
                .stream()
                .filter(MultipleChoiceOption::getIsCorrect)
                .findFirst()
                .orElse(null);

        if (correctOption == null)
            return 0;

        MultipleChoiceAnswer mcAnswer = (MultipleChoiceAnswer) answer.getAnswer();
        return mcAnswer != null && mcAnswer.verifyAnswer(correctOption.getId()) ? question.getPoint().intValue() : 0;
    }

    private int gradeFillInBlank(Question question, AnswerData answer) {
        FillInBlankData fillInBlankData = (FillInBlankData) question.getData();
        List<BlankSegment> blankSegments = fillInBlankData.getSegments()
                .stream()
                .filter(s -> s.getType() == BlankSegment.SegmentType.BLANK)
                .toList();

        if (blankSegments.isEmpty())
            return 0;

        FillInBlankAnswer fillAnswer = (FillInBlankAnswer) answer.getAnswer();
        if (fillAnswer == null || fillAnswer.getBlankAnswers() == null)
            return 0;

        int totalPoint = question.getPoint().intValue();
        int pointPerBlank = totalPoint / blankSegments.size();

        return blankSegments.stream().mapToInt(segment -> {
            String studentAnswer = fillAnswer.getBlankAnswers().get(segment.getId());
            return studentAnswer != null && segment.getAcceptableAnswers().contains(studentAnswer) ? pointPerBlank : 0;
        }).sum();
    }

    private int gradeMatching(Question question, AnswerData answer) {
        MatchingData matchingData = (MatchingData) question.getData();
        List<MatchingPair> pairs = matchingData.getPairs();

        if (pairs == null || pairs.isEmpty())
            return 0;

        MatchingAnswer matchAnswer = (MatchingAnswer) answer.getAnswer();
        if (matchAnswer == null)
            return 0;

        Map<String, String> studentPairs = matchAnswer.getMatchedPairs();
        if (studentPairs == null || studentPairs.isEmpty())
            return 0;

        int totalPoint = question.getPoint().intValue();
        int pointPerPair = totalPoint / pairs.size();

        return pairs.stream().mapToInt(pair -> {
            String leftKey = pair.getLeft() != null && !pair.getLeft().isBlank()
                    ? pair.getLeft()
                    : pair.getLeftImageUrl();
            String rightValue = pair.getRight() != null && !pair.getRight().isBlank()
                    ? pair.getRight()
                    : pair.getRightImageUrl();

            String studentValue = studentPairs.get(leftKey);
            return studentValue != null && studentValue.equals(rightValue) ? pointPerPair : 0;
        }).sum();
    }
}
