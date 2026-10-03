package com.wefit.feedService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private UUID userId;
    private UUID postId;
    private String content;
    private ZonedDateTime createdAt;
}
