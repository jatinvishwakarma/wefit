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
@Document(collection = "workout_plans")
public class WorkoutPlan {
    @Id
    private String id;
    private Long userId;
    
    private String goal;
    private String fitnessLevel;
    private Integer daysPerWeek;
    
    private List<DailyWorkout> schedule;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyWorkout {
        private int day;
        private String focus;
        private List<Exercise> exercises;
        private String notes;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Exercise {
        private String name;
        private String sets;
        private String reps;
        private String rest;
    }
}
