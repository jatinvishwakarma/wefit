package com.wefit.aiService.dto;

import lombok.Data;

@Data
public class NutritionPlanRequest {
    private Integer age;
    private String gender; // MALE, FEMALE
    private Double weightKg;
    private Double heightCm;
    private String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTRA_ACTIVE
    private String goal; // LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT
    private String dietaryPreferences; // e.g., Vegan, Keto, None
}
