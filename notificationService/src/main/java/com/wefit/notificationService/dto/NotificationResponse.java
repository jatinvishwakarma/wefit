package com.wefit.notificationService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private UUID actorId;
    private String type;
    private String message;
    private Boolean isRead;
    private UUID targetId;
    private ZonedDateTime createdAt;
}
