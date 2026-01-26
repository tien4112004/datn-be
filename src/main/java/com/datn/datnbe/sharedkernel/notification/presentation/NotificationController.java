package com.datn.datnbe.sharedkernel.notification.presentation;

import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.notification.dto.AppNotificationDto;
import com.datn.datnbe.sharedkernel.notification.dto.DeviceTokenRequest;
import com.datn.datnbe.sharedkernel.notification.dto.NotificationRequest;
import com.datn.datnbe.sharedkernel.notification.dto.SendNotificationToUsersRequest;
import com.datn.datnbe.sharedkernel.notification.dto.UnreadCountDto;
import com.datn.datnbe.sharedkernel.notification.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/device")
    public ResponseEntity<String> registerDevice(@RequestBody DeviceTokenRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        notificationService.registerDevice(userId, request.getToken());
        return ResponseEntity.ok("Device registered successfully");
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        try {
            String response = notificationService.sendNotification(request);
            return ResponseEntity.ok("Notification sent: " + response);
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.badRequest().body("Failed to send notification: " + e.getMessage());
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
    public ResponseEntity<UnreadCountDto> getUnreadCount(Authentication authentication) {
        String userId = authentication.getName();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(UnreadCountDto.builder().count(count).build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        boolean updated = notificationService.markAsRead(id, userId);
        if (updated) {
            return ResponseEntity.ok("Notification marked as read");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(Authentication authentication) {
        String userId = authentication.getName();
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("Marked " + count + " notifications as read");
    }

    @PostMapping("/send-to-users")
    public ResponseEntity<String> sendNotificationToUsers(@Valid @RequestBody SendNotificationToUsersRequest request) {
        notificationService.sendNotificationToUsers(request);
        return ResponseEntity.ok("Notifications sent to users");
    }
}
