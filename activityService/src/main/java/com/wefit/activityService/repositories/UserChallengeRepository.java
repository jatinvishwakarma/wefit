package com.wefit.activityService.repositories;

import com.wefit.activityService.entities.UserChallenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeRepository extends MongoRepository<UserChallenge, String> {
    List<UserChallenge> findByUserId(Long userId);
    Optional<UserChallenge> findByUserIdAndChallengeId(Long userId, String challengeId);
    List<UserChallenge> findByUserIdAndIsCompletedFalse(Long userId);
}
