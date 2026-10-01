package com.wefit.activityService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChallengeRequest {
    private String title;
    private String description;
    private String goalType; // DISTANCE, CALORIES, DURATION
    private Double targetValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
