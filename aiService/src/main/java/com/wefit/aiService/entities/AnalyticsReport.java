package com.wefit.aiService.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "analytics_reports")
public class AnalyticsReport {
    @Id
    private String id;
    private Long userId;

    private Integer totalActivities;
    private Integer totalDurationMinutes;
    private Integer totalCaloriesBurned;

    private Double burnoutRiskScore; // 0.0 to 10.0
    private String burnoutWarning;

    private String progressAnalysis;
    private String predictiveInsights;

    @CreatedDate
    private LocalDateTime createdAt;
}
