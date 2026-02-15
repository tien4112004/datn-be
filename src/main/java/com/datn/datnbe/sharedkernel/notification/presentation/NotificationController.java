package com.datn.datnbe.sharedkernel.notification.presentation;

import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.notification.dto.AppNotificationDto;
import com.datn.datnbe.sharedkernel.notification.dto.DeviceTokenRequest;
import com.datn.datnbe.sharedkernel.notification.dto.NotificationRequest;
import com.datn.datnbe.sharedkernel.notification.dto.SendNotificationToUsersRequest;
import com.datn.datnbe.sharedkernel.notification.dto.UnreadCountDto;
import com.datn.datnbe.sharedkernel.notification.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/device")
    public ResponseEntity<AppResponseDto<String>> registerDevice(@RequestBody DeviceTokenRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        notificationService.registerDevice(userId, request.getToken());
        return ResponseEntity.ok(AppResponseDto.success("Device registered successfully"));
    }

    @PostMapping("/send")
    public ResponseEntity<AppResponseDto<String>> sendNotification(@RequestBody NotificationRequest request) {
        try {
            String response = notificationService.sendNotification(request);
            return ResponseEntity.ok(AppResponseDto.success("Notification sent: " + response));
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.badRequest()
                    .body(AppResponseDto.success("Failed to send notification: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<AppNotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        PaginatedResponseDto<AppNotificationDto> response = notificationService.getNotificationsForUser(userId,
                pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<AppResponseDto<UnreadCountDto>> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(AppResponseDto.success(UnreadCountDto.builder().count(count).build()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<AppResponseDto<String>> markAsRead(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        boolean updated = notificationService.markAsRead(id, userId);
        if (updated) {
            return ResponseEntity.ok(AppResponseDto.success("Notification marked as read"));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<AppResponseDto<String>> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(AppResponseDto.success("Marked " + count + " notifications as read"));
    }

    @PostMapping("/send-to-users")
    public ResponseEntity<AppResponseDto<String>> sendNotificationToUsers(
            @Valid @RequestBody SendNotificationToUsersRequest request) {
        notificationService.sendNotificationToUsers(request);
        return ResponseEntity.ok(AppResponseDto.success("Notifications sent to users"));
    }

    @PostMapping("/send-me")
    public ResponseEntity<AppResponseDto<String>> sendNotificationToCurrentUser(@RequestBody NotificationRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        SendNotificationToUsersRequest dto = SendNotificationToUsersRequest.builder()
                .userIds(java.util.List.of(userId))
                .title(request.getTitle() != null ? request.getTitle() : "Test notification")
                .body(request.getBody() != null ? request.getBody() : "Test notification from payment-test.html")
                .type(com.datn.datnbe.sharedkernel.notification.enums.NotificationType.SYSTEM)
                .referenceId(null)
                .data(request.getData())
                .build();
        notificationService.sendNotificationToUsers(dto);
        return ResponseEntity.ok(AppResponseDto.success("Notification queued for current user"));
    }
}
