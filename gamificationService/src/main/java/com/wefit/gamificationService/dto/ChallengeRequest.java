package com.wefit.gamificationService.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ChallengeRequest {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String type;

    @NotNull
    @Positive
    private Double targetValue;

    @NotNull
    @Future
    private ZonedDateTime startDate;

    @NotNull
    @Future
    private ZonedDateTime endDate;
}
