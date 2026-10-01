package com.wefit.relationshipService.service;

import com.wefit.relationshipService.dto.CursorPageResponse;
import com.wefit.relationshipService.dto.RelationshipResponseDto;
import com.wefit.relationshipService.model.Relationship;
import com.wefit.relationshipService.model.RelationshipStatus;
import com.wefit.relationshipService.repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RelationshipResponseDto followUser(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        Optional<Relationship> existing = relationshipRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        
        Relationship relationship;
        if (existing.isPresent()) {
            relationship = existing.get();
            if (relationship.getStatus() == RelationshipStatus.BLOCKED) {
                throw new IllegalArgumentException("Cannot follow this user");
            }
            relationship.setStatus(RelationshipStatus.PENDING); // Wait for accept
        } else {
            relationship = Relationship.builder()
                    .followerId(followerId)
                    .followingId(followingId)
                    .status(RelationshipStatus.PENDING) // Wait for accept
                    .build();
        }

        Relationship saved = relationshipRepository.save(relationship);
        
        // Publish to Kafka for feed generation/notifications
        kafkaTemplate.send("user-follow-requested", saved.getId().toString(), 
            String.format("{\"followerId\":\"%s\", \"followingId\":\"%s\"}", followerId, followingId));
            
        return mapToDto(saved);
    }

    public RelationshipResponseDto acceptFollow(UUID followerId, UUID followingId) {
        Relationship relationship = relationshipRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new IllegalArgumentException("Follow request not found"));
            
        if (relationship.getStatus() != RelationshipStatus.PENDING) {
            throw new IllegalStateException("Follow request is not pending");
        }
        
        relationship.setStatus(RelationshipStatus.ACCEPTED);
        Relationship saved = relationshipRepository.save(relationship);
        
        kafkaTemplate.send("user-follow-accepted", saved.getId().toString(), 
            String.format("{\"followerId\":\"%s\", \"followingId\":\"%s\"}", followerId, followingId));
            
        return mapToDto(saved);
    }

    public void rejectFollow(UUID followerId, UUID followingId) {
        relationshipRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .ifPresent(r -> {
                if (r.getStatus() == RelationshipStatus.PENDING) {
                    relationshipRepository.delete(r);
                    kafkaTemplate.send("user-follow-rejected", r.getId().toString(), 
                        String.format("{\"followerId\":\"%s\", \"followingId\":\"%s\"}", followerId, followingId));
                }
            });
    }

    public void unfollowUser(UUID followerId, UUID followingId) {
        relationshipRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .ifPresent(r -> {
                relationshipRepository.delete(r);
                kafkaTemplate.send("user-unfollowed", r.getId().toString(), 
                    String.format("{\"followerId\":\"%s\", \"followingId\":\"%s\"}", followerId, followingId));
            });
    }

    public void blockUser(UUID blockerId, UUID blockedId) {
        Relationship relationship = relationshipRepository.findByFollowerIdAndFollowingId(blockerId, blockedId)
            .orElse(Relationship.builder().followerId(blockerId).followingId(blockedId).build());
            
        relationship.setStatus(RelationshipStatus.BLOCKED);
        relationshipRepository.save(relationship);
        
        // Also remove reciprocal follow if it exists
        relationshipRepository.findByFollowerIdAndFollowingId(blockedId, blockerId)
            .ifPresent(relationshipRepository::delete);
    }

    public CursorPageResponse<RelationshipResponseDto> getFollowers(UUID userId, int size, int page) {
        Page<Relationship> pagedResult = relationshipRepository.findByFollowingIdAndStatus(
            userId, 
            RelationshipStatus.ACCEPTED, 
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        
        return CursorPageResponse.<RelationshipResponseDto>builder()
                .data(pagedResult.getContent().stream().map(this::mapToDto).collect(Collectors.toList()))
                .hasNext(pagedResult.hasNext())
                .nextCursor(pagedResult.hasNext() ? String.valueOf(page + 1) : null)
                .build();
    }

    public CursorPageResponse<RelationshipResponseDto> getFollowing(UUID userId, int size, int page) {
        Page<Relationship> pagedResult = relationshipRepository.findByFollowerIdAndStatus(
            userId, 
            RelationshipStatus.ACCEPTED, 
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return CursorPageResponse.<RelationshipResponseDto>builder()
                .data(pagedResult.getContent().stream().map(this::mapToDto).collect(Collectors.toList()))
                .hasNext(pagedResult.hasNext())
                .nextCursor(pagedResult.hasNext() ? String.valueOf(page + 1) : null)
                .build();
    }
    
    public long getFollowersCount(UUID userId) {
        return relationshipRepository.countByFollowingIdAndStatus(userId, RelationshipStatus.ACCEPTED);
    }
    
    public long getFollowingCount(UUID userId) {
        return relationshipRepository.countByFollowerIdAndStatus(userId, RelationshipStatus.ACCEPTED);
    }

    private RelationshipResponseDto mapToDto(Relationship relationship) {
        return RelationshipResponseDto.builder()
                .id(relationship.getId())
                .followerId(relationship.getFollowerId())
                .followingId(relationship.getFollowingId())
                .status(relationship.getStatus())
                .createdAt(relationship.getCreatedAt())
                .build();
    }
}
