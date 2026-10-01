package com.wefit.aiService.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.wefit.aiService.dto.CursorPageResponse;
import com.wefit.aiService.dto.RecommendationResponseDto;
import com.wefit.aiService.entities.Recommendation;
import com.wefit.aiService.repositories.RecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public CursorPageResponse<RecommendationResponseDto> getUserRecommendations(Long userId, String cursor, int limit) {
        List<Recommendation> recommendations;
        // Fetch one extra to determine hasMore
        int fetchLimit = limit + 1;
        
        if (cursor != null && !cursor.isEmpty()) {
            recommendations = recommendationRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, PageRequest.of(0, fetchLimit));
        } else {
            recommendations = recommendationRepository.findByUserIdOrderByIdDesc(userId, PageRequest.of(0, fetchLimit));
        }
        
        boolean hasMore = recommendations.size() > limit;
        if (hasMore) {
            recommendations.remove(recommendations.size() - 1);
        }
        
        List<RecommendationResponseDto> dtos = recommendations.stream().map(this::mapToDto).collect(Collectors.toList());
        String nextCursor = hasMore && !dtos.isEmpty() ? dtos.get(dtos.size() - 1).getId() : null;
        
        return new CursorPageResponse<>(dtos, nextCursor, hasMore);
    }

    public RecommendationResponseDto getActivityRecommendation(String activityId) {
        Recommendation rec = recommendationRepository.findByActivityId(activityId);
        return rec != null ? mapToDto(rec) : null;
    }
    
    private RecommendationResponseDto mapToDto(Recommendation rec) {
        return RecommendationResponseDto.builder()
                .id(rec.getId())
                .userId(rec.getUserId())
                .activityId(rec.getActivityId())
                .recommendation(rec.getRecommendation())
                .improvements(rec.getImprovements())
                .suggestions(rec.getSuggestions())
                .safetyPrecautions(rec.getSafetyPrecautions())
                .userRating(rec.getUserRating())
                .userFeedback(rec.getUserFeedback())
                .createdAt(rec.getCreatedAt())
                .build();
    }
    
    public void rateRecommendation(String recommendationId, Integer rating, String feedback) {
        recommendationRepository.findById(recommendationId).ifPresent(rec -> {
            rec.setUserRating(rating);
            rec.setUserFeedback(feedback);
            recommendationRepository.save(rec);
        });
    }
}
