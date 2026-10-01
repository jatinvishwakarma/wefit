package com.wefit.moderationService.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private Long reporterId;
    private String contentId;
    private String contentType;
    private String reason;
    private String description;
}
