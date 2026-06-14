package com.wefit.aiService.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.wefit.aiService.entities.Recommendation;
import com.wefit.aiService.repositories.RecommendationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public List<Recommendation> getUserRecommendations(Long userId) {
        return recommendationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    public Recommendation getActivityRecommendation(String activityId) {
        return recommendationRepository.findByActivityId(activityId);
    }
}
