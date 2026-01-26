package com.datn.datnbe.sharedkernel.notification.service;

import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;
import com.datn.datnbe.sharedkernel.notification.dto.AppNotificationDto;
import com.datn.datnbe.sharedkernel.notification.dto.NotificationRequest;
import com.datn.datnbe.sharedkernel.notification.dto.SendNotificationToUsersRequest;
import com.datn.datnbe.sharedkernel.notification.entity.AppNotification;
import com.datn.datnbe.sharedkernel.notification.entity.UserDevice;
import com.datn.datnbe.sharedkernel.notification.repository.AppNotificationRepository;
import com.datn.datnbe.sharedkernel.notification.repository.UserDeviceRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final UserDeviceRepository userDeviceRepository;
    private final AppNotificationRepository appNotificationRepository;

    public NotificationService(UserDeviceRepository userDeviceRepository,
            AppNotificationRepository appNotificationRepository) {
        this.userDeviceRepository = userDeviceRepository;
        this.appNotificationRepository = appNotificationRepository;
    }

    @Transactional
    public void registerDevice(String userId, String token) {
        log.info("Registering device for user: {}, token: {}",
                userId,
                token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");

        userDeviceRepository.findByUserIdAndFcmToken(userId, token).ifPresentOrElse(device -> {
            log.info("Device already exists, updating timestamp");
            device.setUpdatedAt(java.time.LocalDateTime.now());
            userDeviceRepository.save(device);
        }, () -> {
            log.info("Creating new device registration");
            UserDevice newDevice = UserDevice.builder().userId(userId).fcmToken(token).deviceType("UNKNOWN").build();
            UserDevice saved = userDeviceRepository.save(newDevice);
            log.info("Device saved with id: {}", saved.getId());
        });
    }

    public String sendNotification(NotificationRequest request) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        Message.Builder messageBuilder = Message.builder().setToken(request.getToken()).setNotification(notification);

        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(request.getData());
        }

        return FirebaseMessaging.getInstance().send(messageBuilder.build());
    }

    public void sendMulticast(List<String> tokens, NotificationRequest request) throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification);

        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(request.getData());
        }

        FirebaseMessaging.getInstance().sendMulticast(messageBuilder.build());
    }

    @Transactional
    public void sendNotificationToUsers(SendNotificationToUsersRequest request) {
        // Save AppNotification for each userId (batch save)
        List<AppNotification> notifications = request.getUserIds()
                .stream()
                .map(userId -> AppNotification.builder()
                        .userId(userId)
                        .title(request.getTitle())
                        .body(request.getBody())
                        .type(request.getType())
                        .referenceId(request.getReferenceId())
                        .isRead(false)
                        .build())
                .toList();
        appNotificationRepository.saveAll(notifications);

        // Fire FCM async (fire-and-forget)
        sendFcmToUsersAsync(request.getUserIds(), request);
    }

    @Async
    public void sendFcmToUsersAsync(List<String> userIds, SendNotificationToUsersRequest request) {
        try {
            // Collect tokens for all users
            List<String> tokens = userIds.stream()
                    .flatMap(userId -> userDeviceRepository.findAllByUserId(userId).stream())
                    .map(UserDevice::getFcmToken)
                    .distinct()
                    .toList();

            if (tokens.isEmpty()) {
                log.info("No FCM tokens found for users: {}", userIds);
                return;
            }

            // Send multicast notification
            NotificationRequest fcmRequest = NotificationRequest.builder()
                    .title(request.getTitle())
                    .body(request.getBody())
                    .data(request.getData())
                    .build();

            sendMulticast(tokens, fcmRequest);
            log.info("FCM notifications sent to {} devices", tokens.size());
        } catch (Exception e) {
            log.error("Failed to send FCM notifications to users: {}", userIds, e);
        }
    }

    public PaginatedResponseDto<AppNotificationDto> getNotificationsForUser(String userId, Pageable pageable) {
        Page<AppNotification> page = appNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<AppNotificationDto> dtos = page.getContent()
                .stream()
                .map(AppNotificationDto::fromEntity)
                .collect(Collectors.toList());

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return PaginatedResponseDto.<AppNotificationDto>builder().data(dtos).pagination(pagination).build();
    }

    public long getUnreadCount(String userId) {
        return appNotificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public boolean markAsRead(String notificationId, String userId) {
        int updated = appNotificationRepository.markAsRead(notificationId, userId);
        return updated > 0;
    }

    @Transactional
    public int markAllAsRead(String userId) {
        return appNotificationRepository.markAllAsRead(userId);
    }
}
