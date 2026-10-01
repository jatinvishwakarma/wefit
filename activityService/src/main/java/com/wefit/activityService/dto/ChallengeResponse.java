package com.wefit.activityService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChallengeResponse {
    private String id;
    private String title;
    private String description;
    private String goalType;
    private Double targetValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
