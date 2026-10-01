package com.wefit.aiService.dto;

import com.wefit.aiService.entities.NutritionPlan;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NutritionPlanResponse {
    private String id;
    private Long userId;

    private Integer age;
    private String gender;
    private Double weightKg;
    private Double heightCm;
    private String activityLevel;
    private String goal;

    private Double targetCalories;
    private Double proteinGrams;
    private Double carbsGrams;
    private Double fatGrams;

    private List<NutritionPlan.MealSuggestion> mealSuggestions;

    private LocalDateTime createdAt;
}
