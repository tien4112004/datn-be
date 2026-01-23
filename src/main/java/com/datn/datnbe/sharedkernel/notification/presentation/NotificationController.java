package com.datn.datnbe.sharedkernel.notification.presentation;

import com.datn.datnbe.sharedkernel.notification.dto.DeviceTokenRequest;
import com.datn.datnbe.sharedkernel.notification.dto.NotificationRequest;
import com.datn.datnbe.sharedkernel.notification.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/device")
    public ResponseEntity<String> registerDevice(@RequestBody DeviceTokenRequest request,
            org.springframework.security.core.Authentication authentication) {
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
}
