package com.datn.datnbe.sharedkernel.notification.dto;

import com.datn.datnbe.sharedkernel.notification.entity.AppNotification;
import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppNotificationDto {
    private String id;
    private String title;
    private String body;
    private NotificationType type;
    private String referenceId;
    private Boolean isRead;
    private Date createdAt;

    public static AppNotificationDto fromEntity(AppNotification entity) {
        return AppNotificationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .type(entity.getType())
                .referenceId(entity.getReferenceId())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
