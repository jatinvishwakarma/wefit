package com.wefit.feedService.repository;

import com.wefit.feedService.entities.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, UUID> {
    Optional<LikeEntity> findByUserIdAndTargetIdAndTargetType(UUID userId, UUID targetId, String targetType);
}
