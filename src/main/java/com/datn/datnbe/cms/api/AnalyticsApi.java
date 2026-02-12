package com.datn.datnbe.cms.api;

import com.datn.datnbe.cms.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for analytics and dashboard features
 */
public interface AnalyticsApi {

    /**
     * Get calendar events for a teacher across all their classes
     *
     * @param teacherId The teacher's user ID
     * @param startDate Start date for calendar range
     * @param endDate   End date for calendar range
     * @return List of calendar events
     */
    List<CalendarEventDto> getTeacherCalendar(String teacherId, Instant startDate, Instant endDate);

    /**
     * Get calendar events for a student
     *
     * @param studentId The student's user ID
     * @param startDate Start date for calendar range
     * @param endDate   End date for calendar range
     * @return List of calendar events
     */
    List<CalendarEventDto> getStudentCalendar(String studentId, Instant startDate, Instant endDate);

    /**
     * Get grading queue for a teacher
     *
     * @param teacherId The teacher's user ID
     * @param pageable  Pagination parameters
     * @return List of submissions pending grading
     */
    List<GradingQueueItemDto> getGradingQueue(String teacherId, Pageable pageable);

    /**
     * Get performance metrics for a specific class
     *
     * @param classId   The class ID
     * @param teacherId The teacher's user ID (for permission check)
     * @return Class performance metrics
     */
    ClassPerformanceDto getClassPerformance(String classId, String teacherId);

    /**
     * Get at-risk students grouped by class for all teacher's classes
     *
     * @param teacherId The teacher's user ID
     * @return List of classes with their at-risk students
     */
    List<ClassAtRiskStudentsDto> getAtRiskStudents(String teacherId);

    /**
     * Get item analysis for a specific assignment
     *
     * @param assignmentId The assignment ID
     * @param teacherId    The teacher's user ID (for permission check)
     * @return Item analysis with question and topic breakdowns
     */
    ItemAnalysisDto getItemAnalysis(String assignmentId, String teacherId);

    /**
     * Get summary metrics for teacher dashboard
     *
     * @param teacherId The teacher's user ID
     * @return Teacher summary metrics
     */
    TeacherSummaryDto getTeacherSummary(String teacherId);

    /**
     * Get recent activity feed for a teacher
     *
     * @param teacherId The teacher's user ID
     * @param limit     Maximum number of activities to return
     * @return List of recent activities
     */
    List<RecentActivityDto> getRecentActivity(String teacherId, int limit);

    /**
     * Get performance metrics for a student
     *
     * @param studentId The student's user ID
     * @return Student performance metrics
     */
    StudentPerformanceDto getStudentPerformance(String studentId);
}
