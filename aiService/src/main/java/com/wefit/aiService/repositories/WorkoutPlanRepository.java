package com.wefit.aiService.repositories;

import com.wefit.aiService.entities.WorkoutPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutPlanRepository extends MongoRepository<WorkoutPlan, String> {
    List<WorkoutPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
