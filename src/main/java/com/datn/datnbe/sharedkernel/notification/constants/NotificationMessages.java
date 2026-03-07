package com.datn.datnbe.sharedkernel.notification.constants;

import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;

import java.util.Map;

public final class NotificationMessages {

    public record NotificationTemplate(String title, String bodyTemplate) {
    }

    public static final String POST_CREATED_TITLE = "Bài viết mới trong lớp học";
    public static final String ASSIGNMENT_CREATED_TITLE = "Bài tập mới trong lớp học";
    public static final String POST_UPDATED_TITLE = "Bài viết đã được cập nhật";
    public static final String POST_PINNED_TITLE = "Bài viết đã được ghim";
    public static final String POST_UNPINNED_TITLE = "Bài viết đã được bỏ ghim";

    public static final Map<NotificationType, NotificationTemplate> TEMPLATES = Map.of(
            NotificationType.ASSIGNMENT_DEADLINE,
            new NotificationTemplate("Nhắc nhở Hạn chót Bài tập",
                    "Bạn chưa nộp bài tập. Hạn chót là %s. Vui lòng nộp bài trước hạn chót."),
            NotificationType.SHARED_PRESENTATION,
            new NotificationTemplate("Tài nguyên được chia sẻ", "%s đã chia sẻ \"%s\" với bạn."),
            NotificationType.SHARED_MINDMAP,
            new NotificationTemplate("Tài nguyên được chia sẻ", "%s đã chia sẻ \"%s\" với bạn."));

    private NotificationMessages() {
    }
}
