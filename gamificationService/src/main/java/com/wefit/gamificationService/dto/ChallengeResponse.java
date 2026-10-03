package com.wefit.gamificationService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ChallengeResponse {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private Double targetValue;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private ZonedDateTime createdAt;
}
