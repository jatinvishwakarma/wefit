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
    private ActivityType activityType;
    private Integer durationInMinutes;
    private Long userId;
    private int caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
