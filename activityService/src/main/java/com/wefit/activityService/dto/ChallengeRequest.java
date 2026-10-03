package com.wefit.activityService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChallengeRequest {
    @jakarta.validation.constraints.NotBlank(message = "Title is required")
    private String title;
    @jakarta.validation.constraints.NotBlank(message = "Description is required")
    private String description;
    @jakarta.validation.constraints.NotBlank(message = "Goal type is required")
    private String goalType; // DISTANCE, CALORIES, DURATION
    @jakarta.validation.constraints.NotNull(message = "Target value is required")
    @jakarta.validation.constraints.Min(value = 0, message = "Target value cannot be negative")
    private Double targetValue;
    @jakarta.validation.constraints.NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    @jakarta.validation.constraints.NotNull(message = "End date is required")
    private LocalDateTime endDate;
}
