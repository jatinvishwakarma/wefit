package com.wefit.aiService.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "nutrition_plans")
public class NutritionPlan {
    @Id
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

    private List<MealSuggestion> mealSuggestions;

    @CreatedDate
    private LocalDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MealSuggestion {
        private String mealType;
        private String description;
        private Integer calories;
        private String macros;
    }
}
