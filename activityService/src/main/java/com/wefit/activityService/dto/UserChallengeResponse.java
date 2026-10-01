package com.wefit.activityService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserChallengeResponse {
    private String id;
    private Long userId;
    private String challengeId;
    private String challengeTitle;
    private String goalType;
    private Double targetValue;
    private Double progressValue;
    private Double progressPercentage;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private LocalDateTime joinedAt;
}
