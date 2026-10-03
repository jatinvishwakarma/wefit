package com.wefit.gamificationService.service;

import com.wefit.gamificationService.dto.BadgeResponse;
import com.wefit.gamificationService.model.Badge;
import com.wefit.gamificationService.model.UserBadge;
import com.wefit.gamificationService.repository.BadgeRepository;
import com.wefit.gamificationService.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public List<BadgeResponse> getUserBadges(UUID userId) {
        return userBadgeRepository.findByUserId(userId)
                .stream()
                .map(UserBadge::getBadge)
                .map(this::mapToResponse)
                .toList();
    }

    public void awardBadge(UUID userId, UUID badgeId) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found"));

        UserBadge userBadge = UserBadge.builder()
                .userId(userId)
                .badge(badge)
                .build();
        
        userBadgeRepository.save(userBadge);
    }

    private BadgeResponse mapToResponse(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .iconUrl(badge.getIconUrl())
                .type(badge.getType())
                .build();
    }
}
