package com.wefit.aiService.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wefit.aiService.entities.Recommendation;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    List<Recommendation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    Recommendation findByActivityId(String activityId);
}
