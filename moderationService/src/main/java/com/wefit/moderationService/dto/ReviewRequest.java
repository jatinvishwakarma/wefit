package com.wefit.moderationService.dto;

import com.wefit.moderationService.model.ContentState;
import lombok.Data;

@Data
public class ReviewRequest {
    private ContentState action; // e.g., REMOVED, RESTORED
    private String reviewerNotes;
}
