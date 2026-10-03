package com.wefit.feedService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class PostResponse {
    private UUID id;
    private UUID userId;
    private String content;
    private UUID activityId;
    private String mediaUrl;
    private Integer likesCount;
    private Integer commentsCount;
    private ZonedDateTime createdAt;
}
