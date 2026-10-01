package com.wefit.activityService.entities;

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
@Document(collection = "challenges")
public class Challenge {
    @Id
    private String id;
    private String title;
    private String description;
    
    // e.g., "DISTANCE", "CALORIES", "DURATION"
    private String goalType;
    
    private Double targetValue;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
