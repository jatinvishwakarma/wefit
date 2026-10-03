package com.wefit.notificationService.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private UUID actorId;

    @Column(nullable = false, length = 50)
    private String type; // e.g. LIKE, COMMENT, FOLLOW

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private Boolean isRead = false;

    private UUID targetId;

    @CreationTimestamp
    @Column(updatable = false)
    private ZonedDateTime createdAt;
}
