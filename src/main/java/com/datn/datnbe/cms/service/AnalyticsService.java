package com.datn.datnbe.cms.service;

import com.datn.datnbe.auth.api.UserProfileApi;
import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
import com.datn.datnbe.cms.api.AnalyticsApi;
import com.datn.datnbe.student.api.StudentApi;
import com.datn.datnbe.cms.dto.response.*;
import com.datn.datnbe.cms.entity.AssignmentPost;
import com.datn.datnbe.cms.entity.ClassEntity;
import com.datn.datnbe.cms.entity.Post;
import com.datn.datnbe.cms.entity.Submission;
import com.datn.datnbe.cms.entity.answerData.*;
import com.datn.datnbe.cms.repository.AssignmentPostRepository;
import com.datn.datnbe.cms.repository.ClassRepository;
import com.datn.datnbe.cms.repository.PostRepository;
import com.datn.datnbe.cms.repository.SubmissionRepository;
import com.datn.datnbe.document.entity.Question;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.dto.students.ClassEnrollmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService implements AnalyticsApi {

    private final SubmissionRepository submissionRepository;
    private final PostRepository postRepository;
    private final ClassRepository classRepository;
    private final AssignmentPostRepository assignmentPostRepository;
    private final UserProfileApi userProfileApi;
    private final StudentApi studentApi;

    @Override
    public List<CalendarEventDto> getTeacherCalendar(String teacherId, Instant startDate, Instant endDate) {
        List<CalendarEventDto> events = new ArrayList<>();

        // Find all classes owned by teacher
        List<ClassEntity> classes = classRepository.findAll()
                .stream()
                .filter(c -> teacherId.equals(c.getOwnerId()))
                .toList();

        for (ClassEntity classEntity : classes) {
            // Find all posts for this class
            List<Post> posts = postRepository.findAll()
                    .stream()
                    .filter(p -> classEntity.getId().equals(p.getClassId()))
                    .toList();

            for (Post post : posts) {
                // Add deadline events for exercise posts
                if ("Exercise".equals(post.getType()) && post.getDueDate() != null) {
                    Instant dueDate = post.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toInstant();

                    if (isWithinRange(dueDate, startDate, endDate)) {
                        events.add(CalendarEventDto.builder()
                                .id(post.getId())
                                .title(getAssignmentTitle(post))
                                .type(CalendarEventDto.EventType.DEADLINE)
                                .date(dueDate)
                                .classId(classEntity.getId())
                                .className(classEntity.getName())
                                .relatedId(post.getId())
                                .description("Assignment due")
                                .status(getDueStatus(dueDate))
                                .build());

                        // Add grading reminder 24-48 hours after deadline
                        Instant gradingReminder = dueDate.plus(Duration.ofHours(36));
                        if (isWithinRange(gradingReminder, startDate, endDate)) {
                            events.add(CalendarEventDto.builder()
                                    .id(post.getId() + "-grading")
                                    .title("Grade: " + getAssignmentTitle(post))
                                    .type(CalendarEventDto.EventType.GRADING_REMINDER)
                                    .date(gradingReminder)
                                    .classId(classEntity.getId())
                                    .className(classEntity.getName())
                                    .relatedId(post.getId())
                                    .description("Grading reminder")
                                    .status("pending")
                                    .build());
                        }
                    }
                }
            }
        }

        return events.stream().sorted(Comparator.comparing(CalendarEventDto::getDate)).toList();
    }

    @Override
    public List<CalendarEventDto> getStudentCalendar(String studentId, Instant startDate, Instant endDate) {
        List<CalendarEventDto> events = new ArrayList<>();

        // Find all classes the student is enrolled in
        List<ClassEnrollmentDto> enrollments = studentApi.getEnrollmentsByUserId(studentId);

        for (ClassEnrollmentDto enrollment : enrollments) {
            ClassEntity classEntity = classRepository.findById(enrollment.getClassId()).orElse(null);
            if (classEntity == null)
                continue;

            // Find all posts for this class
            List<Post> posts = postRepository.findAll()
                    .stream()
                    .filter(p -> classEntity.getId().equals(p.getClassId()))
                    .toList();

            for (Post post : posts) {
                if ("Exercise".equals(post.getType()) && post.getDueDate() != null) {
                    Instant dueDate = post.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toInstant();

                    if (isWithinRange(dueDate, startDate, endDate)) {
                        // Check if student has submitted
                        List<Submission> submissions = submissionRepository.findByPostId(post.getId())
                                .stream()
                                .filter(s -> studentId.equals(s.getStudentId()))
                                .toList();

                        String status = submissions.isEmpty()
                                ? "pending"
                                : submissions.stream().anyMatch(s -> "graded".equals(s.getStatus()))
                                        ? "completed"
                                        : "submitted";

                        events.add(CalendarEventDto.builder()
                                .id(post.getId())
                                .title(getAssignmentTitle(post))
                                .type(CalendarEventDto.EventType.DEADLINE)
                                .date(dueDate)
                                .classId(classEntity.getId())
                                .className(classEntity.getName())
                                .relatedId(post.getId())
                                .description("Assignment due")
                                .status(status)
                                .build());

                        // Add returned assignment events
                        for (Submission submission : submissions) {
                            if ("graded".equals(submission.getStatus()) && submission.getGradedAt() != null) {
                                Instant gradedDate = submission.getGradedAt()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant();
                                if (isWithinRange(gradedDate, startDate, endDate)) {
                                    events.add(CalendarEventDto.builder()
                                            .id(submission.getId() + "-returned")
                                            .title("Returned: " + getAssignmentTitle(post))
                                            .type(CalendarEventDto.EventType.ASSIGNMENT_RETURNED)
                                            .date(gradedDate)
                                            .classId(classEntity.getId())
                                            .className(classEntity.getName())
                                            .relatedId(submission.getId())
                                            .description("Assignment graded and returned")
                                            .status("completed")
                                            .build());
                                }
                            }
                        }
                    }
                }
            }
        }

        return events.stream().sorted(Comparator.comparing(CalendarEventDto::getDate)).toList();
    }

    @Override
    public List<GradingQueueItemDto> getGradingQueue(String teacherId, Pageable pageable) {
        List<Submission> pendingSubmissions = submissionRepository.findPendingSubmissionsByTeacher(teacherId);
        log.debug("Found {} pending submissions for teacher {}", pendingSubmissions.size(), teacherId);

        // Enrich and convert to DTOs
        List<GradingQueueItemDto> queueItems = new ArrayList<>();
        Map<String, UserMinimalInfoDto> userCache = new HashMap<>();

        for (Submission submission : pendingSubmissions) {
            try {
                log.debug("Processing submission {} (postId={}, studentId={})",
                        submission.getId(),
                        submission.getPostId(),
                        submission.getStudentId());

                Post post = postRepository.findById(submission.getPostId()).orElse(null);
                if (post == null) {
                    log.warn("Post not found for submission {}: postId={}", submission.getId(), submission.getPostId());
                    continue;
                }

                ClassEntity classEntity = classRepository.findById(post.getClassId()).orElse(null);
                if (classEntity == null) {
                    log.warn("Class not found for submission {}: classId={}", submission.getId(), post.getClassId());
                    continue;
                }

                // Get student info - submission.studentId should be the userId (Keycloak ID)
                UserMinimalInfoDto student = userCache.computeIfAbsent(submission.getStudentId(), id -> {
                    try {
                        log.debug("Fetching user profile for studentId: {}", id);
                        return userProfileApi.getUserMinimalInfo(id);
                    } catch (Exception e) {
                        log.error("Failed to fetch user profile for studentId {}: {}", id, e.getMessage());
                        return null;
                    }
                });

                if (student == null) {
                    log.warn("Could not fetch student info for submission {}: studentId={}",
                            submission.getId(),
                            submission.getStudentId());
                    continue;
                }

                long daysSince = Duration
                        .between(submission.getSubmittedAt().toInstant().atZone(ZoneId.systemDefault()).toInstant(),
                                Instant.now())
                        .toDays();

                queueItems.add(GradingQueueItemDto.builder()
                        .submissionId(submission.getId())
                        .assignmentId(submission.getAssignmentId())
                        .assignmentTitle(getAssignmentTitle(post))
                        .postId(post.getId())
                        .classId(classEntity.getId())
                        .className(classEntity.getName())
                        .student(student)
                        .submittedAt(submission.getSubmittedAt().toInstant().atZone(ZoneId.systemDefault()).toInstant())
                        .daysSinceSubmission(daysSince)
                        .status(submission.getStatus())
                        .autoGradedScore(submission.getScore())
                        .maxScore(submission.getMaxScore() != null ? submission.getMaxScore().doubleValue() : null)
                        .build());

                log.debug("Successfully added submission {} to grading queue", submission.getId());
            } catch (Exception e) {
                log.error("Failed to process submission {} for grading queue: {}",
                        submission.getId(),
                        e.getMessage(),
                        e);
            }
        }

        log.info("Returning {} items in grading queue (filtered from {} pending submissions)",
                queueItems.size(),
                pendingSubmissions.size());

        // Sort by submission date (oldest first)
        return queueItems.stream().sorted(Comparator.comparing(GradingQueueItemDto::getSubmittedAt)).toList();
    }

    @Override
    public ClassPerformanceDto getClassPerformance(String classId, String teacherId) {
        // Verify teacher owns the class
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        if (!teacherId.equals(classEntity.getOwnerId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Get all enrollments
        List<ClassEnrollmentDto> enrollments = studentApi.getEnrollmentsByClassId(classId);

        int totalStudents = enrollments.size();
        long activeStudents = enrollments.stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();

        // Get all submissions for this class
        List<Submission> submissions = submissionRepository.findByClassId(classId);

        // Deduplicate: use best submission per student per post
        List<Submission> bestSubmissions = getBestSubmissionsPerStudentPerPost(submissions);

        // Calculate participation rate
        List<Post> exercisePosts = postRepository.findAll()
                .stream()
                .filter(p -> classId.equals(p.getClassId()) && "Exercise".equals(p.getType()))
                .toList();

        double participationRate = 0.0;
        if (!exercisePosts.isEmpty() && totalStudents > 0) {
            long totalExpected = (long) exercisePosts.size() * totalStudents;
            long uniqueSubmissions = bestSubmissions.size();
            participationRate = (uniqueSubmissions * 100.0) / totalExpected;
        }

        // Calculate average score as percentage
        Double averageScore = bestSubmissions.stream()
                .mapToDouble(s -> (s.getScore() / s.getMaxScore()) * 100.0)
                .average()
                .orElse(0.0);

        // Grade distribution
        Map<String, Long> gradeDistribution = calculateGradeDistribution(bestSubmissions);

        // At-risk students
        List<AtRiskStudentDto> atRiskStudents = identifyAtRiskStudentsForClass(classId, enrollments);

        // Recent assignments
        List<AssignmentSummaryDto> recentAssignments = exercisePosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .limit(5)
                .map(post -> createAssignmentSummary(post, totalStudents))
                .toList();

        // Engagement metrics
        ClassPerformanceDto.EngagementMetrics engagement = calculateEngagement(classId, submissions);

        return ClassPerformanceDto.builder()
                .classId(classId)
                .className(classEntity.getName())
                .totalStudents(totalStudents)
                .activeStudents((int) activeStudents)
                .participationRate(participationRate)
                .averageScore(averageScore)
                .gradeDistribution(gradeDistribution)
                .atRiskStudents(atRiskStudents)
                .recentAssignments(recentAssignments)
                .engagement(engagement)
                .build();
    }

    @Override
    public List<ClassAtRiskStudentsDto> getAtRiskStudents(String teacherId) {
        log.debug("Getting at-risk students grouped by class for teacher: {}", teacherId);

        // Get all classes owned by teacher
        List<ClassEntity> classes = classRepository.findAll()
                .stream()
                .filter(c -> teacherId.equals(c.getOwnerId()))
                .toList();

        log.debug("Found {} classes for teacher {}", classes.size(), teacherId);

        // Optimization 5: Process classes in parallel for better performance
        List<ClassAtRiskStudentsDto> result = classes.parallelStream().map(classEntity -> {
            try {
                List<ClassEnrollmentDto> enrollments = studentApi.getEnrollmentsByClassId(classEntity.getId());
                int totalStudents = enrollments.size();

                List<AtRiskStudentDto> atRiskStudents = identifyAtRiskStudentsForClass(classEntity.getId(),
                        enrollments);

                // Sort at-risk students by average score (lowest first)
                atRiskStudents = atRiskStudents.stream()
                        .sorted(Comparator.comparing(AtRiskStudentDto::getAverageScore))
                        .toList();

                return ClassAtRiskStudentsDto.builder()
                        .classId(classEntity.getId())
                        .className(classEntity.getName())
                        .totalStudents(totalStudents)
                        .atRiskCount(atRiskStudents.size())
                        .atRiskStudents(atRiskStudents)
                        .build();
            } catch (Exception e) {
                log.error("Failed to process at-risk students for class {}: {}",
                        classEntity.getId(),
                        e.getMessage(),
                        e);
                return null;
            }
        }).filter(Objects::nonNull).toList();

        log.debug("Returning at-risk students for {} classes", result.size());
        return result;
    }

    @Override
    public ItemAnalysisDto getItemAnalysis(String assignmentId, String teacherId) {
        // Get assignment post
        AssignmentPost assignment = assignmentPostRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Verify ownership through post
        Post post = postRepository.findAll()
                .stream()
                .filter(p -> assignmentId.equals(p.getAssignmentId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        ClassEntity classEntity = classRepository.findById(post.getClassId())
                .orElseThrow(() -> new AppException(ErrorCode.CLASS_NOT_FOUND));

        if (!teacherId.equals(classEntity.getOwnerId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Get all submissions
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .filter(s -> "graded".equals(s.getStatus()))
                .toList();

        if (submissions.isEmpty()) {
            return ItemAnalysisDto.builder()
                    .assignmentId(assignmentId)
                    .assignmentTitle(assignment.getTitle())
                    .totalSubmissions(0)
                    .questionAnalysis(new ArrayList<>())
                    .topicAnalysis(new ArrayList<>())
                    .build();
        }

        // Analyze each question
        List<ItemAnalysisDto.QuestionAnalysis> questionAnalysis = analyzeQuestions(assignment, submissions);

        // Analyze by topic
        List<ItemAnalysisDto.TopicAnalysis> topicAnalysis = analyzeTopics(assignment, submissions);

        return ItemAnalysisDto.builder()
                .assignmentId(assignmentId)
                .assignmentTitle(assignment.getTitle())
                .totalSubmissions(submissions.size())
                .questionAnalysis(questionAnalysis)
                .topicAnalysis(topicAnalysis)
                .build();
    }

    @Override
    public TeacherSummaryDto getTeacherSummary(String teacherId) {
        // Get all classes
        List<ClassEntity> classes = classRepository.findAll()
                .stream()
                .filter(c -> teacherId.equals(c.getOwnerId()))
                .toList();

        int totalClasses = classes.size();

        // Get total students
        int totalStudents = 0;
        for (ClassEntity classEntity : classes) {
            totalStudents += (int) studentApi.getTotalEnrollmentCount(classEntity.getId());
        }

        // Get total assignments
        int totalAssignments = 0;
        for (ClassEntity classEntity : classes) {
            totalAssignments += (int) postRepository.findAll()
                    .stream()
                    .filter(p -> classEntity.getId().equals(p.getClassId()) && "Exercise".equals(p.getType()))
                    .count();
        }

        // Pending grading
        int pendingGrading = submissionRepository.findPendingSubmissionsByTeacher(teacherId).size();

        // Get all submissions across all classes
        List<Submission> allSubmissions = new ArrayList<>();
        for (ClassEntity classEntity : classes) {
            allSubmissions.addAll(submissionRepository.findByClassId(classEntity.getId()));
        }

        // Deduplicate: use best submission per student per post
        List<Submission> bestSubmissions = getBestSubmissionsPerStudentPerPost(allSubmissions);

        // Average class score as percentage
        Double averageClassScore = bestSubmissions.stream()
                .mapToDouble(s -> (s.getScore() / s.getMaxScore()) * 100.0)
                .average()
                .orElse(0.0);

        // Engagement rate (24h) - keep using allSubmissions because we want to count recent activity
        Instant yesterday = Instant.now().minus(Duration.ofHours(24));
        long recentSubmissions = allSubmissions.stream()
                .filter(s -> s.getSubmittedAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .isAfter(yesterday))
                .count();
        double engagementRate24h = totalStudents > 0 ? (recentSubmissions * 100.0) / totalStudents : 0.0;

        // Overall grade distribution
        Map<String, Long> overallGradeDistribution = calculateGradeDistribution(bestSubmissions);

        return TeacherSummaryDto.builder()
                .totalClasses(totalClasses)
                .totalStudents(totalStudents)
                .totalAssignments(totalAssignments)
                .pendingGrading(pendingGrading)
                .averageClassScore(averageClassScore)
                .engagementRate24h(engagementRate24h)
                .overallGradeDistribution(overallGradeDistribution)
                .comparison(null) // TODO: Implement period comparison
                .build();
    }

    @Override
    public List<RecentActivityDto> getRecentActivity(String teacherId, int limit) {
        List<Submission> recentSubmissions = submissionRepository.findRecentSubmissionsByTeacher(teacherId, limit);
        Map<String, UserMinimalInfoDto> userCache = new HashMap<>();

        return recentSubmissions.stream().map(submission -> {
            try {
                Post post = postRepository.findById(submission.getPostId()).orElse(null);
                if (post == null)
                    return null;

                ClassEntity classEntity = classRepository.findById(post.getClassId()).orElse(null);
                if (classEntity == null)
                    return null;

                UserMinimalInfoDto student = userCache.computeIfAbsent(submission.getStudentId(),
                        userProfileApi::getUserMinimalInfo);

                RecentActivityDto.ActivityType activityType = RecentActivityDto.ActivityType.SUBMISSION;
                if ("graded".equals(submission.getStatus())) {
                    activityType = RecentActivityDto.ActivityType.GRADING_COMPLETED;
                } else if (post.getDueDate() != null && submission.getSubmittedAt().after(post.getDueDate())) {
                    activityType = RecentActivityDto.ActivityType.LATE_SUBMISSION;
                }

                return RecentActivityDto.builder()
                        .id(submission.getId())
                        .type(activityType)
                        .student(student)
                        .assignmentTitle(getAssignmentTitle(post))
                        .assignmentId(submission.getAssignmentId())
                        .className(classEntity.getName())
                        .classId(classEntity.getId())
                        .timestamp(submission.getSubmittedAt().toInstant().atZone(ZoneId.systemDefault()).toInstant())
                        .score(submission.getScore())
                        .status(submission.getStatus())
                        .build();
            } catch (Exception e) {
                log.warn("Failed to process submission {} for recent activity", submission.getId(), e);
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public StudentPerformanceDto getStudentPerformance(String studentId) {
        // Get all submissions by student
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);

        // Group submissions by postId and get the best/latest graded submission for each assignment
        Map<String, Submission> bestSubmissionPerPost = submissions.stream()
                .filter(s -> "graded".equals(s.getStatus()) && s.getScore() != null && s.getMaxScore() != null
                        && s.getMaxScore() > 0)
                .collect(Collectors.toMap(Submission::getPostId,
                        submission -> submission,
                        // If multiple submissions for same post, keep the one with highest score
                        (s1, s2) -> s1.getScore() > s2.getScore() ? s1 : s2));

        // Overall average as percentage - calculated from best submission per assignment
        Double overallAverage = bestSubmissionPerPost.values()
                .stream()
                .mapToDouble(s -> (s.getScore() / s.getMaxScore()) * 100.0)
                .average()
                .orElse(0.0);

        // Completed assignments = unique posts with graded submissions
        int completedAssignments = bestSubmissionPerPost.size();

        // Get all available assignments for student's classes
        int totalAssignments = calculateTotalAssignmentsForStudent(studentId);

        double completionRate = totalAssignments > 0 ? (completedAssignments * 100.0) / totalAssignments : 0.0;

        // Pending and overdue
        List<Post> pendingPosts = findPendingAssignmentsForStudent(studentId, submissions);
        int pendingAssignments = (int) pendingPosts.stream()
                .filter(p -> (p.getDueDate() == null || p.getDueDate().after(Date.from(Instant.now()))))
                .count();
        int overdueAssignments = (int) pendingPosts.stream()
                .filter(p -> p.getDueDate() != null && p.getDueDate().before(Date.from(Instant.now())))
                .count();

        // Grade distribution - based on best submission per assignment
        Map<String, Long> gradeDistribution = calculateGradeDistribution(
                new ArrayList<>(bestSubmissionPerPost.values()));

        return StudentPerformanceDto.builder()
                .overallAverage(overallAverage)
                .completedAssignments(completedAssignments)
                .totalAssignments(totalAssignments)
                .completionRate(completionRate)
                .pendingAssignments(pendingAssignments)
                .overdueAssignments(overdueAssignments)
                .classSummaries(new ArrayList<>()) // TODO: Implement class summaries
                .performanceTrends(new ArrayList<>()) // TODO: Implement trends
                .gradeDistribution(gradeDistribution)
                .build();
    }

    // Helper methods

    /**
     * Filters submissions to get the best (highest score) submission per student per post.
     * This ensures students with multiple attempts are only counted once with their best score.
     *
     * @param submissions List of all submissions
     * @return List of best submissions per student per post
     */
    private List<Submission> getBestSubmissionsPerStudentPerPost(List<Submission> submissions) {
        return submissions.stream()
                .filter(s -> "graded".equals(s.getStatus()) && s.getScore() != null && s.getMaxScore() != null
                        && s.getMaxScore() > 0)
                .collect(Collectors.toMap(s -> s.getStudentId() + ":" + s.getPostId(), // Composite key
                        submission -> submission,
                        // Keep submission with highest score
                        (s1, s2) -> s1.getScore() > s2.getScore() ? s1 : s2))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    private boolean isWithinRange(Instant date, Instant start, Instant end) {
        return (start == null || !date.isBefore(start)) && (end == null || !date.isAfter(end));
    }

    private String getDueStatus(Instant dueDate) {
        Instant now = Instant.now();
        if (dueDate.isBefore(now)) {
            return "overdue";
        } else if (dueDate.isBefore(now.plus(Duration.ofDays(1)))) {
            return "due-soon";
        }
        return "upcoming";
    }

    private String getAssignmentTitle(Post post) {
        if (post.getAssignmentId() != null) {
            try {
                AssignmentPost assignment = assignmentPostRepository.findById(post.getAssignmentId()).orElse(null);
                if (assignment != null && assignment.getTitle() != null) {
                    return assignment.getTitle();
                }
            } catch (Exception e) {
                log.warn("Could not fetch assignment title", e);
            }
        }
        return "Assignment";
    }

    /**
     * Calculates grade distribution from submissions.
     *
     * IMPORTANT: This method expects pre-deduplicated submissions
     * (best score per student per assignment).
     *
     * @param submissions List of submissions (should be deduplicated by caller)
     * @return Map of grade letters to counts
     */
    private Map<String, Long> calculateGradeDistribution(List<Submission> submissions) {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("A", 0L);
        distribution.put("B", 0L);
        distribution.put("C", 0L);
        distribution.put("D", 0L);
        distribution.put("F", 0L);

        for (Submission submission : submissions) {
            if ("graded".equals(submission.getStatus()) && submission.getScore() != null
                    && submission.getMaxScore() != null && submission.getMaxScore() > 0) {
                // Convert to percentage
                double scorePercentage = (submission.getScore() / submission.getMaxScore()) * 100.0;
                String grade;
                if (scorePercentage >= 90)
                    grade = "A";
                else if (scorePercentage >= 80)
                    grade = "B";
                else if (scorePercentage >= 70)
                    grade = "C";
                else if (scorePercentage >= 60)
                    grade = "D";
                else
                    grade = "F";

                distribution.put(grade, distribution.get(grade) + 1);
            }
        }

        return distribution;
    }

    private List<AtRiskStudentDto> identifyAtRiskStudentsForClass(String classId,
            List<ClassEnrollmentDto> enrollments) {
        List<AtRiskStudentDto> atRiskStudents = new ArrayList<>();
        ClassEntity classEntity = classRepository.findById(classId).orElse(null);
        if (classEntity == null) {
            log.warn("Class not found: {}", classId);
            return atRiskStudents;
        }

        // Optimization 1: Fetch all exercise posts for this class once
        List<Post> exercisePosts = postRepository.findAll()
                .stream()
                .filter(p -> classId.equals(p.getClassId()) && "Exercise".equals(p.getType()))
                .toList();

        int totalAssignments = exercisePosts.size();
        if (totalAssignments == 0) {
            log.debug("No exercise posts found for class: {}", classId);
            return atRiskStudents;
        }

        Set<String> exercisePostIds = exercisePosts.stream().map(Post::getId).collect(Collectors.toSet());
        List<Submission> allSubmissions = submissionRepository.findByClassId(classId);

        Map<String, List<Submission>> submissionsByStudent = allSubmissions.stream()
                .filter(s -> exercisePostIds.contains(s.getPostId()))
                .collect(Collectors.groupingBy(Submission::getStudentId));

        Map<String, Post> postMap = exercisePosts.stream().collect(Collectors.toMap(Post::getId, p -> p));

        List<String> userIds = enrollments.stream()
                .map(ClassEnrollmentDto::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, UserMinimalInfoDto> userProfileMap = new HashMap<>();
        for (String userId : userIds) {
            try {
                userProfileMap.put(userId, userProfileApi.getUserMinimalInfo(userId));
            } catch (Exception e) {
                log.warn("Failed to fetch user profile for userId: {}", userId, e);
            }
        }

        for (ClassEnrollmentDto enrollment : enrollments) {
            try {
                String studentUserId = enrollment.getUserId();
                if (studentUserId == null) {
                    continue;
                }

                // Use cached user profile
                UserMinimalInfoDto studentInfo = userProfileMap.get(studentUserId);
                if (studentInfo == null) {
                    continue;
                }

                // Get submissions from pre-fetched map (no query!)
                List<Submission> studentSubmissions = submissionsByStudent.getOrDefault(studentUserId, List.of());

                // Deduplicate: get best submission per post for this student
                Map<String, Submission> bestSubmissionPerPost = studentSubmissions.stream()
                        .filter(s -> "graded".equals(s.getStatus()) && s.getScore() != null && s.getMaxScore() != null
                                && s.getMaxScore() > 0)
                        .collect(Collectors.toMap(Submission::getPostId,
                                submission -> submission,
                                (s1, s2) -> s1.getScore() > s2.getScore() ? s1 : s2));

                // Calculate average as percentage (score/maxScore * 100)
                double averageScore = bestSubmissionPerPost.values().stream().mapToDouble(s -> {
                    var avg = (s.getScore() / s.getMaxScore()) * 100.0;
                    log.info(
                            "Average score for student {} with max score is {} and original score {} and avg score is {}",
                            studentUserId,
                            s.getMaxScore(),
                            s.getScore(),
                            avg);
                    return avg;
                }).average().orElse(0.0);

                int missedSubmissions = totalAssignments - bestSubmissionPerPost.size();
                // Use pre-fetched postMap instead of querying database
                long lateSubmissions = studentSubmissions.stream().filter(s -> {
                    Post post = postMap.get(s.getPostId());
                    return post != null && post.getDueDate() != null && s.getSubmittedAt().after(post.getDueDate());
                }).map(Submission::getPostId).distinct().count();

                double completionRate = (bestSubmissionPerPost.size() * 100.0) / totalAssignments;

                // Determine if at-risk
                AtRiskStudentDto.RiskLevel riskLevel = null;
                String reason = null;

                if (averageScore < 60 || missedSubmissions >= 2 || completionRate < 50) {
                    if (averageScore < 40 || missedSubmissions >= 3) {
                        riskLevel = AtRiskStudentDto.RiskLevel.CRITICAL;
                        reason = "Very low performance and/or multiple missing assignments";
                    } else if (averageScore < 60 || missedSubmissions >= 2) {
                        riskLevel = AtRiskStudentDto.RiskLevel.HIGH;
                        reason = "Below passing grade or missing multiple assignments";
                    } else {
                        riskLevel = AtRiskStudentDto.RiskLevel.MEDIUM;
                        reason = "Low completion rate";
                    }

                    Instant lastSubmission = studentSubmissions.stream()
                            .map(s -> s.getSubmittedAt().toInstant().atZone(ZoneId.systemDefault()).toInstant())
                            .max(Instant::compareTo)
                            .orElse(null);

                    atRiskStudents.add(AtRiskStudentDto.builder()
                            .student(studentInfo)
                            .averageScore(averageScore)
                            .missedSubmissions(missedSubmissions)
                            .lateSubmissions((int) lateSubmissions)
                            .totalAssignments(totalAssignments)
                            .riskLevel(riskLevel)
                            .build());
                }
            } catch (Exception e) {
                log.error("Failed to analyze student for at-risk detection: enrollmentId={}, error={}",
                        enrollment.getEnrollmentId(),
                        e.getMessage(),
                        e);
            }
        }

        log.debug("Identified {} at-risk students in class {}", atRiskStudents.size(), classId);
        return atRiskStudents;
    }

    private AssignmentSummaryDto createAssignmentSummary(Post post, int totalStudents) {
        List<Submission> allSubmissions = submissionRepository.findByPostId(post.getId());

        // Deduplicate by student (since this is a single post, only need to dedupe by student)
        Map<String, Submission> bestSubmissionPerStudent = allSubmissions.stream()
                .filter(s -> "graded".equals(s.getStatus()) && s.getScore() != null && s.getMaxScore() != null
                        && s.getMaxScore() > 0)
                .collect(Collectors.toMap(Submission::getStudentId,
                        submission -> submission,
                        (s1, s2) -> s1.getScore() > s2.getScore() ? s1 : s2));

        List<Submission> submissions = new ArrayList<>(bestSubmissionPerStudent.values());

        long gradedCount = submissions.size();

        Double averageScore = submissions.stream()
                .mapToDouble(s -> (s.getScore() / s.getMaxScore()) * 100.0)
                .average()
                .orElse(0.0);

        double participationRate = totalStudents > 0 ? (submissions.size() * 100.0) / totalStudents : 0.0;

        Instant dueDate = post.getDueDate() != null
                ? post.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toInstant()
                : null;

        return AssignmentSummaryDto.builder()
                .assignmentId(post.getAssignmentId())
                .title(getAssignmentTitle(post))
                .dueDate(dueDate)
                .totalSubmissions(submissions.size())
                .gradedSubmissions((int) gradedCount)
                .averageScore(averageScore)
                .participationRate(participationRate)
                .build();
    }

    private ClassPerformanceDto.EngagementMetrics calculateEngagement(String classId, List<Submission> submissions) {
        Instant now = Instant.now();
        Instant yesterday = now.minus(Duration.ofHours(24));
        Instant weekAgo = now.minus(Duration.ofDays(7));

        long last24h = submissions.stream()
                .filter(s -> s.getSubmittedAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .isAfter(yesterday))
                .map(Submission::getStudentId)
                .distinct()
                .count();

        long last7days = submissions.stream()
                .filter(s -> s.getSubmittedAt().toInstant().atZone(ZoneId.systemDefault()).toInstant().isAfter(weekAgo))
                .map(Submission::getStudentId)
                .distinct()
                .count();

        long totalStudents = studentApi.getTotalEnrollmentCount(classId);

        double avg24h = totalStudents > 0 ? (last24h * 100.0) / totalStudents : 0.0;
        double avg7days = totalStudents > 0 ? (last7days * 100.0) / totalStudents : 0.0;

        double avgSubmissions = totalStudents > 0 ? (double) submissions.size() / totalStudents : 0.0;

        return ClassPerformanceDto.EngagementMetrics.builder()
                .last24Hours(avg24h)
                .last7Days(avg7days)
                .avgSubmissionsPerStudent(avgSubmissions)
                .build();
    }

    /**
     * Analyzes question performance across ALL student attempts.
     *
     * Note: This method intentionally includes ALL submissions, not just best scores,
     * because understanding question difficulty requires analyzing all attempts,
     * including failures that led to retries.
     */
    private List<ItemAnalysisDto.QuestionAnalysis> analyzeQuestions(AssignmentPost assignment,
            List<Submission> submissions) {
        if (assignment.getQuestions() == null) {
            return new ArrayList<>();
        }

        List<ItemAnalysisDto.QuestionAnalysis> analysis = new ArrayList<>();

        for (Question question : assignment.getQuestions()) {
            int correctCount = 0;
            int incorrectCount = 0;
            double totalScore = 0.0;
            Map<String, Integer> optionDistribution = new HashMap<>();

            for (Submission submission : submissions) {
                if (submission.getQuestions() == null)
                    continue;

                AnswerData answer = submission.getQuestions()
                        .stream()
                        .filter(a -> question.getId().equals(a.getId()))
                        .findFirst()
                        .orElse(null);

                if (answer != null) {
                    if (answer.getPoint() != null) {
                        totalScore += answer.getPoint();
                        if (answer.getPoint() >= (question.getPoint() != null ? question.getPoint() * 0.5 : 0)) {
                            correctCount++;
                        } else {
                            incorrectCount++;
                        }
                    }

                    // Track option distribution for MC questions
                    if (answer.getAnswer() instanceof MultipleChoiceAnswer mcAnswer) {
                        String selected = mcAnswer.getId(); // The id field contains the selected option
                        optionDistribution.put(selected, optionDistribution.getOrDefault(selected, 0) + 1);
                    }
                }
            }

            int totalAnswers = correctCount + incorrectCount;
            double averageScore = totalAnswers > 0 ? totalScore / totalAnswers : 0.0;
            double successRate = totalAnswers > 0 ? (correctCount * 100.0) / totalAnswers : 0.0;

            String difficulty = "Medium";
            if (successRate >= 80)
                difficulty = "Easy";
            else if (successRate < 50)
                difficulty = "Hard";

            analysis.add(ItemAnalysisDto.QuestionAnalysis.builder()
                    .questionId(question.getId())
                    .questionTitle(question.getTitle())
                    .questionType(question.getType() != null ? question.getType().name() : "UNKNOWN")
                    .averageScore(averageScore)
                    .maxPoints(question.getPoint() != null ? question.getPoint() : 0.0)
                    .successRate(successRate)
                    .correctCount(correctCount)
                    .incorrectCount(incorrectCount)
                    .difficulty(difficulty)
                    .optionDistribution(optionDistribution.isEmpty() ? null : optionDistribution)
                    .build());
        }

        return analysis;
    }

    /**
     * Analyzes topic performance across ALL student attempts.
     *
     * Note: This method intentionally includes ALL submissions, not just best scores,
     * to provide accurate topic difficulty metrics.
     */
    private List<ItemAnalysisDto.TopicAnalysis> analyzeTopics(AssignmentPost assignment, List<Submission> submissions) {
        // Group questions by topic
        if (assignment.getQuestions() == null) {
            return new ArrayList<>();
        }

        Map<String, List<Question>> questionsByTopic = assignment.getQuestions()
                .stream()
                .filter(q -> q.getTopicId() != null)
                .collect(Collectors.groupingBy(Question::getTopicId));

        List<ItemAnalysisDto.TopicAnalysis> topicAnalysis = new ArrayList<>();

        for (Map.Entry<String, List<Question>> entry : questionsByTopic.entrySet()) {
            String topicId = entry.getKey();
            List<Question> topicQuestions = entry.getValue();

            double totalScore = 0.0;
            int totalAnswers = 0;
            int correctAnswers = 0;

            for (Question question : topicQuestions) {
                for (Submission submission : submissions) {
                    if (submission.getQuestions() == null)
                        continue;

                    AnswerData answer = submission.getQuestions()
                            .stream()
                            .filter(a -> question.getId().equals(a.getId()))
                            .findFirst()
                            .orElse(null);

                    if (answer != null && answer.getPoint() != null) {
                        totalScore += answer.getPoint();
                        totalAnswers++;
                        if (answer.getPoint() >= (question.getPoint() != null ? question.getPoint() * 0.5 : 0)) {
                            correctAnswers++;
                        }
                    }
                }
            }

            double averageScore = totalAnswers > 0 ? totalScore / totalAnswers : 0.0;
            double successRate = totalAnswers > 0 ? (correctAnswers * 100.0) / totalAnswers : 0.0;

            topicAnalysis.add(ItemAnalysisDto.TopicAnalysis.builder()
                    .topicId(topicId)
                    .topicName("Topic " + topicId) // TODO: Fetch actual topic name
                    .averageScore(averageScore)
                    .successRate(successRate)
                    .questionCount(topicQuestions.size())
                    .build());
        }

        return topicAnalysis;
    }

    private int calculateTotalAssignmentsForStudent(String studentId) {
        // Find all classes student is enrolled in
        try {
            List<ClassEnrollmentDto> enrollments = studentApi.getEnrollmentsByUserId(studentId);

            int total = 0;
            for (ClassEnrollmentDto enrollment : enrollments) {
                total += (int) postRepository.findAll()
                        .stream()
                        .filter(p -> enrollment.getClassId().equals(p.getClassId()) && "Exercise".equals(p.getType()))
                        .count();
            }
            return total;
        } catch (Exception e) {
            log.warn("Failed to calculate total assignments for student", e);
            return 0;
        }
    }

    private List<Post> findPendingAssignmentsForStudent(String studentId, List<Submission> submissions) {
        Set<String> submittedPostIds = submissions.stream().map(Submission::getPostId).collect(Collectors.toSet());

        try {
            List<ClassEnrollmentDto> enrollments = studentApi.getEnrollmentsByUserId(studentId);

            List<Post> pendingPosts = new ArrayList<>();
            for (ClassEnrollmentDto enrollment : enrollments) {
                pendingPosts.addAll(postRepository.findAll()
                        .stream()
                        .filter(p -> enrollment.getClassId().equals(p.getClassId()) && "Exercise".equals(p.getType())
                                && !submittedPostIds.contains(p.getId()))
                        .toList());
            }
            return pendingPosts;
        } catch (Exception e) {
            log.warn("Failed to find pending assignments for student", e);
            return new ArrayList<>();
        }
    }
}
