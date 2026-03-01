package com.datn.datnbe.cms.scheduler;

import com.datn.datnbe.cms.entity.Post;
import com.datn.datnbe.cms.repository.PostRepository;
import com.datn.datnbe.sharedkernel.notification.dto.SendNotificationToUsersRequest;
import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;
import com.datn.datnbe.sharedkernel.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentDeadlineReminderScheduler {

    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    @Async("taskScheduler")
    public void sendDeadlineReminderOneDay() {
        log.info("Starting deadline reminder job - checking for assignments due tomorrow at 9 AM");
        try {
            sendDeadlineReminders();
        } catch (Exception e) {
            log.error("Error in deadline reminder scheduler", e);
        }
    }

    @Transactional(readOnly = true)
    protected void sendDeadlineReminders() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayStart = calendar.getTimeInMillis();

        long tomorrowStart = todayStart + (24 * 60 * 60 * 1000);

        long tomorrowEnd = tomorrowStart + (24 * 60 * 60 * 1000) - 1;

        log.info("Checking for assignments with deadlines tomorrow (from {} to {})", 
                new Date(tomorrowStart), new Date(tomorrowEnd));

        // Fetch all posts (paginated to avoid memory issues)
        int page = 0;
        int pageSize = 50;
        boolean hasMore = true;

        while (hasMore) {
            Page<Post> posts = postRepository.findAll(PageRequest.of(page, pageSize));

            for (Post post : posts.getContent()) {
                // Check if post is an assignment with a deadline
                if (post.getAssignmentId() != null && !post.getAssignmentId().isEmpty() &&
                        post.getDueDate() != null) {

                    long dueTime = post.getDueDate().getTime();

                    // Check if deadline is tomorrow
                    if (dueTime >= tomorrowStart && dueTime <= tomorrowEnd) {
                        log.info("Found assignment due tomorrow: post={}, dueDate={}", 
                                post.getId(), post.getDueDate());
                        sendReminderAsync(post);
                    }
                }
            }

            hasMore = posts.hasNext();
            page++;
        }

        log.info("Deadline reminder job completed");
    }

    /**
     * Send reminder notification asynchronously to avoid blocking the job.
     */
    @Async("taskScheduler")
    protected void sendReminderAsync(Post post) {
        try {
            log.info("Sending deadline reminder for post: {}", post.getId());

            // Get students who haven't submitted
            List<String> studentIds = postRepository.findStudentsWithoutSubmissionByPost(
                    post.getClassId(), post.getId());

            if (studentIds == null || studentIds.isEmpty()) {
                return;
            }

            // Create reminder notification
            String assignmentTitle = "Nhắc nhở Hạn chót Bài tập";
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            String message = "Bạn chưa nộp bài tập. Hạn chót là " +
                    dateFormat.format(post.getDueDate()) +
                    ". Vui lòng nộp bài trước hạn chót.";

            SendNotificationToUsersRequest notificationRequest = SendNotificationToUsersRequest.builder()
                    .userIds(studentIds)
                    .title(assignmentTitle)
                    .body(message)
                    .type(NotificationType.ASSIGNMENT_DEADLINE)
                    .referenceId(post.getId())
                    .build();

            notificationService.sendNotificationToUsers(notificationRequest);
            log.info("Deadline reminder sent to {} students for post: {}", studentIds.size(), post.getId());

        } catch (Exception e) {
            log.error("Failed to send deadline reminder for post: {}", post.getId(), e);
        }
    }
}
