package com.wefit.activityService.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.wefit.activityService.entities.ActivityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequestDto {
    @jakarta.validation.constraints.NotNull(message = "Activity type is required")
    private ActivityType activityType;
    @jakarta.validation.constraints.NotNull(message = "Duration is required")
    @jakarta.validation.constraints.Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationInMinutes;
    @jakarta.validation.constraints.NotNull(message = "User ID is required")
    private Long userId;
    @jakarta.validation.constraints.Min(value = 0, message = "Calories cannot be negative")
    private int caloriesBurned;
    @jakarta.validation.constraints.NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
