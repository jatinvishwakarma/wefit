package com.wefit.feedService.service;

import com.wefit.feedService.dto.*;
import com.wefit.feedService.entities.Comment;
import com.wefit.feedService.entities.LikeEntity;
import com.wefit.feedService.entities.Post;
import com.wefit.feedService.repository.CommentRepository;
import com.wefit.feedService.repository.LikeRepository;
import com.wefit.feedService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostResponse createPost(UUID userId, PostRequest request) {
        Post post = Post.builder()
                .userId(userId)
                .content(request.getContent())
                .activityId(request.getActivityId())
                .mediaUrl(request.getMediaUrl())
                .likesCount(0)
                .commentsCount(0)
                .build();
        
        post = postRepository.save(post);
        return mapToPostResponse(post);
    }

    public Page<PostResponse> getFeed(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToPostResponse);
    }

    public Page<PostResponse> getUserPosts(UUID userId, Pageable pageable) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToPostResponse);
    }

    @Transactional
    public CommentResponse addComment(UUID userId, UUID postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        Comment comment = Comment.builder()
                .userId(userId)
                .postId(postId)
                .content(request.getContent())
                .build();
        
        comment = commentRepository.save(comment);
        
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        
        return mapToCommentResponse(comment);
    }

    public Page<CommentResponse> getComments(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
                .map(this::mapToCommentResponse);
    }

    @Transactional
    public void toggleLike(UUID userId, UUID targetId, String targetType) {
        Optional<LikeEntity> existingLike = likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
        
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            if ("POST".equals(targetType)) {
                postRepository.findById(targetId).ifPresent(post -> {
                    post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
                    postRepository.save(post);
                });
            }
            // Add comment like count logic if required later
        } else {
            LikeEntity like = LikeEntity.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .targetType(targetType)
                    .build();
            likeRepository.save(like);
            if ("POST".equals(targetType)) {
                postRepository.findById(targetId).ifPresent(post -> {
                    post.setLikesCount(post.getLikesCount() + 1);
                    postRepository.save(post);
                });
            }
        }
    }

    private PostResponse mapToPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .activityId(post.getActivityId())
                .mediaUrl(post.getMediaUrl())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
