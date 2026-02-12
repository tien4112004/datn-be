package com.datn.datnbe.cms.presentation;

import com.datn.datnbe.cms.api.AnalyticsApi;
import com.datn.datnbe.cms.dto.response.*;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard and analytics endpoints for teachers and students")
public class AnalyticsController {

    private final AnalyticsApi analyticsApi;
    private final SecurityContextUtils securityContextUtils;

    @GetMapping("/teacher/calendar")
    @Operation(summary = "Get calendar events for teacher", description = "Returns calendar events including deadlines, grading reminders, and scheduled posts")
    public ResponseEntity<AppResponseDto<List<CalendarEventDto>>> getTeacherCalendar(
            @Parameter(description = "Start date (YYYY-MM-DD format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String teacherId = securityContextUtils.getCurrentUserId();

        // Default to current month if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }

        // Convert LocalDate to Instant (start of day in system default timezone)
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // End of endDate

        List<CalendarEventDto> events = analyticsApi.getTeacherCalendar(teacherId, startInstant, endInstant);
        return ResponseEntity.ok(AppResponseDto.success(events));
    }

    @GetMapping("/student/calendar")
    @Operation(summary = "Get calendar events for student", description = "Returns calendar events including assignment deadlines and graded assignments")
    public ResponseEntity<AppResponseDto<List<CalendarEventDto>>> getStudentCalendar(
            @Parameter(description = "Start date (YYYY-MM-DD format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String studentId = securityContextUtils.getCurrentUserId();

        // Default to current month if dates not provided
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }

        // Convert LocalDate to Instant (start of day in system default timezone)
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // End of endDate

        List<CalendarEventDto> events = analyticsApi.getStudentCalendar(studentId, startInstant, endInstant);
        return ResponseEntity.ok(AppResponseDto.success(events));
    }

    @GetMapping("/teacher/grading-queue")
    @Operation(summary = "Get grading queue for teacher", description = "Returns list of submissions pending grading across all teacher's classes")
    public ResponseEntity<AppResponseDto<List<GradingQueueItemDto>>> getGradingQueue(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {

        String teacherId = securityContextUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        List<GradingQueueItemDto> queue = analyticsApi.getGradingQueue(teacherId, pageable);
        return ResponseEntity.ok(AppResponseDto.success(queue));
    }

    @GetMapping("/teacher/classes/{classId}/performance")
    @Operation(summary = "Get class performance metrics", description = "Returns comprehensive performance metrics for a specific class")
    public ResponseEntity<AppResponseDto<ClassPerformanceDto>> getClassPerformance(
            @Parameter(description = "Class ID") @PathVariable String classId) {

        String teacherId = securityContextUtils.getCurrentUserId();
        ClassPerformanceDto performance = analyticsApi.getClassPerformance(classId, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(performance));
    }

    @GetMapping("/teacher/students/at-risk")
    @Operation(summary = "Get at-risk students grouped by class", description = "Returns at-risk students grouped by class for all teacher's classes in a single request")
    public ResponseEntity<AppResponseDto<List<ClassAtRiskStudentsDto>>> getAtRiskStudents() {
        String teacherId = securityContextUtils.getCurrentUserId();
        List<ClassAtRiskStudentsDto> atRiskStudents = analyticsApi.getAtRiskStudents(teacherId);
        return ResponseEntity.ok(AppResponseDto.success(atRiskStudents));
    }

    @GetMapping("/teacher/summary")
    @Operation(summary = "Get teacher dashboard summary", description = "Returns summary metrics for teacher dashboard")
    public ResponseEntity<AppResponseDto<TeacherSummaryDto>> getTeacherSummary() {
        String teacherId = securityContextUtils.getCurrentUserId();
        TeacherSummaryDto summary = analyticsApi.getTeacherSummary(teacherId);
        return ResponseEntity.ok(AppResponseDto.success(summary));
    }

    @GetMapping("/teacher/recent-activity")
    @Operation(summary = "Get recent activity feed", description = "Returns recent submission and grading activity across all teacher's classes")
    public ResponseEntity<AppResponseDto<List<RecentActivityDto>>> getRecentActivity(
            @Parameter(description = "Maximum number of activities to return") @RequestParam(defaultValue = "20") int limit) {

        String teacherId = securityContextUtils.getCurrentUserProfileId();
        List<RecentActivityDto> activities = analyticsApi.getRecentActivity(teacherId, limit);
        return ResponseEntity.ok(AppResponseDto.success(activities));
    }

    @GetMapping("/student/performance")
    @Operation(summary = "Get student performance metrics", description = "Returns comprehensive performance metrics for the current student")
    public ResponseEntity<AppResponseDto<StudentPerformanceDto>> getStudentPerformance() {
        String studentId = securityContextUtils.getCurrentUserProfileId();
        StudentPerformanceDto performance = analyticsApi.getStudentPerformance(studentId);
        return ResponseEntity.ok(AppResponseDto.success(performance));
    }

    @GetMapping("/assignments/{assignmentId}/item-analysis")
    @Operation(summary = "Get item analysis for assignment", description = "Returns question-level and topic-level analysis showing which parts were most challenging")
    public ResponseEntity<AppResponseDto<ItemAnalysisDto>> getItemAnalysis(
            @Parameter(description = "Assignment ID") @PathVariable String assignmentId) {

        String teacherId = securityContextUtils.getCurrentUserProfileId();
        ItemAnalysisDto analysis = analyticsApi.getItemAnalysis(assignmentId, teacherId);
        return ResponseEntity.ok(AppResponseDto.success(analysis));
    }
}
