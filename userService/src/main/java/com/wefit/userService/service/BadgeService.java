package com.wefit.userService.service;

import com.wefit.userService.entities.Badge;
import com.wefit.userService.entities.User;
import com.wefit.userService.entities.UserBadge;
import com.wefit.userService.repository.BadgeRepository;
import com.wefit.userService.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    @Transactional
    public void evaluateBadges(User user) {
        // Evaluate consistency badges
        if (user.getCurrentStreak() >= 7) {
            unlockBadge(user.getId(), "7-Day Streak");
        }
        if (user.getCurrentStreak() >= 30) {
            unlockBadge(user.getId(), "30-Day Streak");
        }
        
        // Evaluate level badges
        if (user.getLevel() >= 10) {
            unlockBadge(user.getId(), "Level 10 Achiever");
        }
    }

    private void unlockBadge(Long userId, String badgeName) {
        Optional<Badge> badgeOpt = badgeRepository.findByName(badgeName);
        if (badgeOpt.isPresent()) {
            Badge badge = badgeOpt.get();
            if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                UserBadge userBadge = UserBadge.builder()
                        .userId(userId)
                        .badge(badge)
                        .build();
                userBadgeRepository.save(userBadge);
            }
        }
    }
}
