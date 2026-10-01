package com.wefit.relationshipService.controller;

import com.wefit.relationshipService.dto.CursorPageResponse;
import com.wefit.relationshipService.dto.RelationshipResponseDto;
import com.wefit.relationshipService.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.RequestMapping;
import org.springframework.web.bind.RequestParam;
import org.springframework.web.bind.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<RelationshipResponseDto> followUser(
            @PathVariable UUID followerId,
            @PathVariable UUID followingId) {
        return ResponseEntity.ok(relationshipService.followUser(followerId, followingId));
    }

    @PostMapping("/{followerId}/follow/{followingId}/accept")
    public ResponseEntity<RelationshipResponseDto> acceptFollow(
            @PathVariable UUID followerId,
            @PathVariable UUID followingId) {
        return ResponseEntity.ok(relationshipService.acceptFollow(followerId, followingId));
    }

    @PostMapping("/{followerId}/follow/{followingId}/reject")
    public ResponseEntity<Void> rejectFollow(
            @PathVariable UUID followerId,
            @PathVariable UUID followingId) {
        relationshipService.rejectFollow(followerId, followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followerId}/unfollow/{followingId}")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable UUID followerId,
            @PathVariable UUID followingId) {
        relationshipService.unfollowUser(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{blockerId}/block/{blockedId}")
    public ResponseEntity<Void> blockUser(
            @PathVariable UUID blockerId,
            @PathVariable UUID blockedId) {
        relationshipService.blockUser(blockerId, blockedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<CursorPageResponse<RelationshipResponseDto>> getFollowers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(relationshipService.getFollowers(userId, size, page));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<CursorPageResponse<RelationshipResponseDto>> getFollowing(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(relationshipService.getFollowing(userId, size, page));
    }
    
    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable UUID userId) {
        return ResponseEntity.ok(relationshipService.getFollowersCount(userId));
    }
    
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable UUID userId) {
        return ResponseEntity.ok(relationshipService.getFollowingCount(userId));
    }
}
