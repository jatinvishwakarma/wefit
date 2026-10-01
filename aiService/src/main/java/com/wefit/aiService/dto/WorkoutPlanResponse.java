package com.wefit.aiService.dto;

import com.wefit.aiService.entities.WorkoutPlan;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkoutPlanResponse {
    private String id;
    private Long userId;
    private String goal;
    private String fitnessLevel;
    private Integer daysPerWeek;
    private List<WorkoutPlan.DailyWorkout> schedule;
    private LocalDateTime createdAt;
}
