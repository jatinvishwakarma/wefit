package com.wefit.gamificationService.service;

import com.wefit.gamificationService.dto.UserLevelResponse;
import com.wefit.gamificationService.model.UserLevel;
import com.wefit.gamificationService.repository.UserLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserLevelService {
    private final UserLevelRepository userLevelRepository;

    public UserLevelResponse getUserLevel(UUID userId) {
        UserLevel userLevel = userLevelRepository.findById(userId)
                .orElseGet(() -> userLevelRepository.save(UserLevel.builder().userId(userId).build()));
        return mapToResponse(userLevel);
    }

    @Transactional
    public void addXp(UUID userId, Long xpToAdd) {
        UserLevel userLevel = userLevelRepository.findById(userId)
                .orElseGet(() -> UserLevel.builder().userId(userId).build());
        
        userLevel.setXp(userLevel.getXp() + xpToAdd);
        userLevel.setLevel((int) (userLevel.getXp() / 1000) + 1); // Simple level logic: 1 level per 1000 XP
        
        userLevelRepository.save(userLevel);
    }

    @Transactional
    public void updateStreak(UUID userId) {
        UserLevel userLevel = userLevelRepository.findById(userId)
                .orElseGet(() -> UserLevel.builder().userId(userId).build());
        
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = userLevel.getLastActivityDate();
        
        if (lastActivity == null) {
            userLevel.setCurrentStreak(1);
        } else if (lastActivity.equals(today.minusDays(1))) {
            userLevel.setCurrentStreak(userLevel.getCurrentStreak() + 1);
        } else if (lastActivity.isBefore(today.minusDays(1))) {
            userLevel.setCurrentStreak(1); // Streak broken
        }
        
        if (userLevel.getCurrentStreak() > userLevel.getLongestStreak()) {
            userLevel.setLongestStreak(userLevel.getCurrentStreak());
        }
        
        userLevel.setLastActivityDate(today);
        userLevelRepository.save(userLevel);
    }

    private UserLevelResponse mapToResponse(UserLevel userLevel) {
        return UserLevelResponse.builder()
                .userId(userLevel.getUserId())
                .xp(userLevel.getXp())
                .level(userLevel.getLevel())
                .currentStreak(userLevel.getCurrentStreak())
                .longestStreak(userLevel.getLongestStreak())
                .lastActivityDate(userLevel.getLastActivityDate())
                .build();
    }
}
