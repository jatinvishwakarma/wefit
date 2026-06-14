package com.wefit.aiService.entities;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("Activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    @Id
    private String id;
    private ActivityType activityType;
    private Integer durationInMinutes;
    private Long userId;
    private int caloriesBurned;
    private LocalDateTime startTime;
    @Field("metrics")
    private Map<String, Object> additionalMetrics;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}