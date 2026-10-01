package com.wefit.aiService.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResponseDto {
    private String id;
    private Long userId;
    private String activityId;
    private String recommendation;
    private List<String> improvements;
    private List<String> suggestions;
    private List<String> safetyPrecautions;
    
    private Integer userRating;
    private String userFeedback;
    
    private LocalDateTime createdAt;
}
