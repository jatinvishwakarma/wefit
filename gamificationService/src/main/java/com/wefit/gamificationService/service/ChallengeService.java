package com.wefit.gamificationService.service;

import com.wefit.gamificationService.dto.ChallengeRequest;
import com.wefit.gamificationService.dto.ChallengeResponse;
import com.wefit.gamificationService.model.Challenge;
import com.wefit.gamificationService.model.ChallengeMember;
import com.wefit.gamificationService.repository.ChallengeMemberRepository;
import com.wefit.gamificationService.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;

    public ChallengeResponse createChallenge(ChallengeRequest request) {
        Challenge challenge = Challenge.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .targetValue(request.getTargetValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        
        challenge = challengeRepository.save(challenge);
        return mapToResponse(challenge);
    }

    public Page<ChallengeResponse> getChallenges(Pageable pageable) {
        return challengeRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public void joinChallenge(UUID challengeId, UUID userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        
        challengeMemberRepository.findByChallengeIdAndUserId(challengeId, userId)
                .ifPresent(m -> {
                    throw new IllegalStateException("User already joined this challenge");
                });
        
        ChallengeMember member = ChallengeMember.builder()
                .challenge(challenge)
                .userId(userId)
                .build();
        
        challengeMemberRepository.save(member);
    }

    private ChallengeResponse mapToResponse(Challenge challenge) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .targetValue(challenge.getTargetValue())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .createdAt(challenge.getCreatedAt())
                .build();
    }
}
