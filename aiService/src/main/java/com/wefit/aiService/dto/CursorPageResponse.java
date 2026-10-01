package com.wefit.aiService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponse<T> {
    private List<T> data;
    private String nextCursor;
    private boolean hasMore;
}
