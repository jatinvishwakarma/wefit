package com.wefit.activityService.repositories;

import com.wefit.activityService.entities.Challenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChallengeRepository extends MongoRepository<Challenge, String> {
    List<Challenge> findByEndDateGreaterThanEqual(LocalDateTime date);
}
