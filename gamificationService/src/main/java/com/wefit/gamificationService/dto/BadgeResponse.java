package com.wefit.gamificationService.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BadgeResponse {
    private UUID id;
    private String name;
    private String description;
    private String iconUrl;
    private String type;
}
