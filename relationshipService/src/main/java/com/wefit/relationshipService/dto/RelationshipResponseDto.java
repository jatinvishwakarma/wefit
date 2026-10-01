package com.wefit.relationshipService.dto;

import com.wefit.relationshipService.model.RelationshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationshipResponseDto {
    private UUID id;
    private UUID followerId;
    private UUID followingId;
    private RelationshipStatus status;
    private ZonedDateTime createdAt;
}
