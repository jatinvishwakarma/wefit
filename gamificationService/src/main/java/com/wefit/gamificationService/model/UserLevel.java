package com.wefit.gamificationService.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLevel {
    @Id
    private UUID userId;

    @Column(nullable = false)
    @Builder.Default
    private Long xp = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    private LocalDate lastActivityDate;
}
