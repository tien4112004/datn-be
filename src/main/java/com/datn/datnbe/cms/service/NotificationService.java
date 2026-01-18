package com.datn.datnbe.cms.service;

import com.datn.datnbe.cms.dto.NotificationRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class NotificationService {

    private final com.datn.datnbe.cms.repository.UserDeviceRepository userDeviceRepository;

    public NotificationService(com.datn.datnbe.cms.repository.UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    public void registerDevice(String userId, String token) {
        userDeviceRepository.findByUserIdAndFcmToken(userId, token)
                .ifPresentOrElse(
                        device -> {
                            device.setUpdatedAt(java.time.LocalDateTime.now());
                            userDeviceRepository.save(device);
                        },
                        () -> {
                            com.datn.datnbe.cms.entity.UserDevice newDevice = com.datn.datnbe.cms.entity.UserDevice
                                    .builder()
                                    .userId(userId)
                                    .fcmToken(token)
                                    .deviceType("UNKNOWN") // Simplify for now
                                    .build();
                            userDeviceRepository.save(newDevice);
                        });
    }

    public String sendNotification(NotificationRequest request) throws FirebaseMessagingException {
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        Message.Builder messageBuilder = Message.builder()
                .setToken(request.getToken())
                .setNotification(notification);

        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(request.getData());
        }

        return FirebaseMessaging.getInstance().send(messageBuilder.build());
    }

    public void sendMulticast(java.util.List<String> tokens, NotificationRequest request)
            throws FirebaseMessagingException {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();

        com.google.firebase.messaging.MulticastMessage.Builder messageBuilder = com.google.firebase.messaging.MulticastMessage
                .builder()
                .addAllTokens(tokens)
                .setNotification(notification);

        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(request.getData());
        }

        FirebaseMessaging.getInstance().sendMulticast(messageBuilder.build());
    }
}
