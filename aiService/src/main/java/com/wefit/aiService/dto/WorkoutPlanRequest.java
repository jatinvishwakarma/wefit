package com.wefit.aiService.dto;

import lombok.Data;

@Data
public class WorkoutPlanRequest {
    private String goal; // e.g., Weight Loss, Muscle Gain, Endurance
    private String fitnessLevel; // e.g., Beginner, Intermediate, Advanced
    private Integer daysPerWeek;
    private String additionalPreferences;
}
