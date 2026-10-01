package com.wefit.moderationService.dto;

import com.wefit.moderationService.model.ContentState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String contentId;
    private String contentType;
    private String reason;
    private String description;
    private ContentState state;
    private LocalDateTime createdAt;
}
