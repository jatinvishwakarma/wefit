package com.wefit.aiService.dto;

import lombok.Data;

@Data
public class AnalyticsReportRequest {
    // In a real microservices architecture, this data would be fetched from ActivityService via Feign or Kafka,
    // but we can simulate it with a direct payload for generation for now.
    private Integer totalActivities;
    private Integer totalDurationMinutes;
    private Integer totalCaloriesBurned;
    private Integer daysSinceLastRest;
    private Double averageHeartRate;
    private String goal;
}
