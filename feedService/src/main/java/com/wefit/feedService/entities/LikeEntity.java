package com.wefit.feedService.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID targetId; // can be post id or comment id

    @Column(nullable = false, length = 20)
    private String targetType; // POST or COMMENT

    @CreationTimestamp
    @Column(updatable = false)
    private ZonedDateTime createdAt;
}
