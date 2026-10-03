package com.wefit.feedService.controller;

import com.wefit.feedService.dto.CommentRequest;
import com.wefit.feedService.dto.CommentResponse;
import com.wefit.feedService.dto.PostRequest;
import com.wefit.feedService.dto.PostResponse;
import com.wefit.feedService.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Feed and Posts", description = "Endpoints for managing user feeds, posts, comments, and likes")
public class FeedController {

    private final FeedService feedService;

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new post", description = "Creates a new post on the user's feed")
    public PostResponse createPost(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PostRequest request) {
        return feedService.createPost(userId, request);
    }

    @GetMapping("/feed")
    @Operation(summary = "Get user feed", description = "Retrieves a paginated feed of posts")
    public Page<PostResponse> getFeed(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return feedService.getFeed(PageRequest.of(page, size));
    }

    @GetMapping("/users/{userId}/posts")
    @Operation(summary = "Get user's posts", description = "Retrieves all posts created by a specific user")
    public Page<PostResponse> getUserPosts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return feedService.getUserPosts(userId, PageRequest.of(page, size));
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add comment", description = "Adds a comment to a post")
    public CommentResponse addComment(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request) {
        return feedService.addComment(userId, postId, request);
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Get comments", description = "Retrieves comments for a specific post")
    public Page<CommentResponse> getComments(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return feedService.getComments(postId, PageRequest.of(page, size));
    }

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "Toggle post like", description = "Likes or unlikes a post")
    public ResponseEntity<Void> togglePostLike(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID postId) {
        feedService.toggleLike(userId, postId, "POST");
        return ResponseEntity.ok().build();
    }
}
