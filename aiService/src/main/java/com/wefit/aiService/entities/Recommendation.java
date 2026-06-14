package com.wefit.aiService.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ai_recommendations")
public class Recommendation {
    @Id
    private String id;
    private Long userId;
    private String activityId;
    private String recommendation;
    private List<String> improvements;
    private List<String> suggestions;
    private List<String> safetyPrecautions;
    @CreatedDate
    private LocalDateTime createdAt;
}
