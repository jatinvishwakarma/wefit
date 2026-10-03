package com.wefit.notificationService.service;

import com.wefit.notificationService.dto.NotificationResponse;
import com.wefit.notificationService.entities.Notification;
import com.wefit.notificationService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    public void markAsRead(UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    // For receiving Kafka events in the future and pushing them via WS
    public void pushNotification(Notification notification) {
        notification = notificationRepository.save(notification);
        NotificationResponse response = mapToResponse(notification);
        messagingTemplate.convertAndSendToUser(
                notification.getUserId().toString(), 
                "/queue/notifications", 
                response
        );
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .actorId(notification.getActorId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
