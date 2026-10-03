package com.wefit.gamificationService.repository;

import com.wefit.gamificationService.model.ChallengeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, UUID> {
    Optional<ChallengeMember> findByChallengeIdAndUserId(UUID challengeId, UUID userId);
    List<ChallengeMember> findByChallengeIdOrderByCurrentScoreDesc(UUID challengeId);
}
