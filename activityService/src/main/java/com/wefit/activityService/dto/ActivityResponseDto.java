package com.wefit.activityService.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.wefit.activityService.entities.Activity;
import com.wefit.activityService.entities.ActivityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDto {
    private String id;
    private ActivityType activityType;
    private Integer durationInMinutes;
    private Long userId;
    private int caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivityResponseDto toDto(Activity savedActivity) {
        return ActivityResponseDto.builder()
                .id(savedActivity.getId())
                .activityType(savedActivity.getActivityType())
                .durationInMinutes(savedActivity.getDurationInMinutes())
                .userId(savedActivity.getUserId())
                .caloriesBurned(savedActivity.getCaloriesBurned())
                .startTime(savedActivity.getStartTime())
                .additionalMetrics(savedActivity.getAdditionalMetrics())
                .createdAt(savedActivity.getCreatedAt())
                .updatedAt(savedActivity.getUpdatedAt())
                .build();
    }
}
