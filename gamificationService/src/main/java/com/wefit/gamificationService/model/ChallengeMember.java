package com.wefit.gamificationService.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "challenge_members", uniqueConstraints = {@UniqueConstraint(columnNames = {"challenge_id", "user_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    @Builder.Default
    private Double currentScore = 0.0;

    @CreationTimestamp
    @Column(updatable = false)
    private ZonedDateTime joinedAt;
}
