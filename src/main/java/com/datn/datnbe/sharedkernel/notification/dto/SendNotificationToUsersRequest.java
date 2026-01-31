package com.datn.datnbe.sharedkernel.notification.dto;

import com.datn.datnbe.sharedkernel.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationToUsersRequest {

    @NotEmpty(message = "User IDs cannot be empty")
    private List<String> userIds;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    private String body;

    private NotificationType type;

    private String referenceId;

    private Map<String, String> data;
}
