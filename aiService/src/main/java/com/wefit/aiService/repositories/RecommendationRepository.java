package com.wefit.aiService.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import com.wefit.aiService.entities.Recommendation;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    List<Recommendation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query(value = "{ 'userId' : ?0, '_id' : { $lt : ?1 } }", sort = "{ '_id' : -1 }")
    List<Recommendation> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, String id, Pageable pageable);

    @Query(value = "{ 'userId' : ?0 }", sort = "{ '_id' : -1 }")
    List<Recommendation> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Recommendation findByActivityId(String activityId);
}
