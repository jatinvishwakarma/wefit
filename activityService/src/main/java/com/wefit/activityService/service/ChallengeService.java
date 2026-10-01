package com.wefit.activityService.service;

import com.wefit.activityService.dto.ChallengeRequest;
import com.wefit.activityService.dto.ChallengeResponse;
import com.wefit.activityService.dto.UserChallengeResponse;
import com.wefit.activityService.entities.Challenge;
import com.wefit.activityService.entities.UserChallenge;
import com.wefit.activityService.repositories.ChallengeRepository;
import com.wefit.activityService.repositories.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    // ─── Admin: Create a Challenge ─────────────────────────────────────────────

    public ChallengeResponse createChallenge(ChallengeRequest request) {
        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .goalType(request.getGoalType())
                .targetValue(request.getTargetValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .build();

        Challenge saved = challengeRepository.save(challenge);
        log.info("Challenge created: {}", saved.getId());
        return mapToChallengeResponse(saved);
    }

    // ─── List Active Challenges ────────────────────────────────────────────────

    public List<ChallengeResponse> getActiveChallenges() {
        return challengeRepository.findByEndDateGreaterThanEqual(LocalDateTime.now())
                .stream()
                .map(this::mapToChallengeResponse)
                .toList();
    }

    // ─── User: Join a Challenge ────────────────────────────────────────────────

    public UserChallengeResponse joinChallenge(Long userId, String challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

        if (userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId).isPresent()) {
            throw new IllegalStateException("User has already joined this challenge.");
        }

        UserChallenge userChallenge = UserChallenge.builder()
                .userId(userId)
                .challengeId(challengeId)
                .progressValue(0.0)
                .isCompleted(false)
                .joinedAt(LocalDateTime.now())
                .build();

        UserChallenge saved = userChallengeRepository.save(userChallenge);
        log.info("User {} joined challenge {}", userId, challengeId);

        return mapToUserChallengeResponse(saved, challenge);
    }

    // ─── User: Update Progress ─────────────────────────────────────────────────

    public UserChallengeResponse updateProgress(Long userId, String challengeId, Double newValue) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalStateException("User has not joined this challenge."));

        userChallenge.setProgressValue(userChallenge.getProgressValue() + newValue);

        if (!userChallenge.getIsCompleted() && userChallenge.getProgressValue() >= challenge.getTargetValue()) {
            userChallenge.setIsCompleted(true);
            userChallenge.setCompletedAt(LocalDateTime.now());
            log.info("User {} completed challenge {}!", userId, challengeId);

            // Publish completion event to Kafka
            kafkaTemplate.send("challenge-completed", Map.of(
                    "userId", userId,
                    "challengeId", challengeId,
                    "challengeTitle", challenge.getTitle()
            ));
        }

        UserChallenge saved = userChallengeRepository.save(userChallenge);
        return mapToUserChallengeResponse(saved, challenge);
    }

    // ─── User: My Challenges ───────────────────────────────────────────────────

    public List<UserChallengeResponse> getMyChallenges(Long userId) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserId(userId);
        return userChallenges.stream().map(uc -> {
            Challenge challenge = challengeRepository.findById(uc.getChallengeId()).orElse(null);
            return mapToUserChallengeResponse(uc, challenge);
        }).toList();
    }

    // ─── Mappers ───────────────────────────────────────────────────────────────

    private ChallengeResponse mapToChallengeResponse(Challenge c) {
        return ChallengeResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .goalType(c.getGoalType())
                .targetValue(c.getTargetValue())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private UserChallengeResponse mapToUserChallengeResponse(UserChallenge uc, Challenge c) {
        double target = c != null ? c.getTargetValue() : 1.0;
        double progress = uc.getProgressValue() != null ? uc.getProgressValue() : 0.0;
        double percentage = Math.min(100.0, (progress / target) * 100);

        return UserChallengeResponse.builder()
                .id(uc.getId())
                .userId(uc.getUserId())
                .challengeId(uc.getChallengeId())
                .challengeTitle(c != null ? c.getTitle() : null)
                .goalType(c != null ? c.getGoalType() : null)
                .targetValue(target)
                .progressValue(progress)
                .progressPercentage(percentage)
                .isCompleted(uc.getIsCompleted())
                .completedAt(uc.getCompletedAt())
                .joinedAt(uc.getJoinedAt())
                .build();
    }
}
