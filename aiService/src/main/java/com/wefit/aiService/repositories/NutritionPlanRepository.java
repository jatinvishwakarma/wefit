package com.wefit.aiService.repositories;

import com.wefit.aiService.entities.NutritionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NutritionPlanRepository extends MongoRepository<NutritionPlan, String> {
    List<NutritionPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
