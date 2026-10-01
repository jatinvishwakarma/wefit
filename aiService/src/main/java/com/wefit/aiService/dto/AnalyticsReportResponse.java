package com.wefit.aiService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalyticsReportResponse {
    private String id;
    private Long userId;

    private Integer totalActivities;
    private Integer totalDurationMinutes;
    private Integer totalCaloriesBurned;

    private Double burnoutRiskScore; 
    private String burnoutWarning;

    private String progressAnalysis;
    private String predictiveInsights;

    private LocalDateTime createdAt;
}
