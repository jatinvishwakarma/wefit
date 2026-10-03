package com.wefit.gamificationService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserLevelResponse {
    private UUID userId;
    private Long xp;
    private Integer level;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;
}
