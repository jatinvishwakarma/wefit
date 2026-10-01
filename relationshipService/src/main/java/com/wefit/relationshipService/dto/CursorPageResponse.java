package com.wefit.relationshipService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {
    private List<T> data;
    private String nextCursor;
    private boolean hasNext;
}
