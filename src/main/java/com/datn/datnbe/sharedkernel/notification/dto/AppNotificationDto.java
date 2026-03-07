package com.datn.datnbe.sharedkernel.notification.dto;

import com.datn.datnbe.sharedkernel.notification.entity.AppNotification;
import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Date;
import java.util.Map;

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
    private Map<String, String> data;
    private Boolean isRead;
    private Date createdAt;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static AppNotificationDto fromEntity(AppNotification entity) {
        Map<String, String> data = null;
        if (entity.getData() != null) {
            try {
                data = MAPPER.readValue(entity.getData(), new TypeReference<>() {
                });
            } catch (Exception ignored) {
            }
        }
        return AppNotificationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .type(entity.getType())
                .referenceId(entity.getReferenceId())
                .data(data)
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
