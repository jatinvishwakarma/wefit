package com.wefit.userService.service;

import com.wefit.userService.entities.User;
import com.wefit.userService.entities.XpHistory;
import com.wefit.userService.repository.UserRepository;
import com.wefit.userService.repository.XpHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class GamificationService {
    
    private final UserRepository userRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final BadgeService badgeService;
    
    private static final int DAILY_XP_LIMIT = 1000;
    
    public GamificationService(UserRepository userRepository, XpHistoryRepository xpHistoryRepository, BadgeService badgeService) {
        this.userRepository = userRepository;
        this.xpHistoryRepository = xpHistoryRepository;
        this.badgeService = badgeService;
    }

    @Transactional
    public void grantXp(Long userId, int xpAmount, String source, String description) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return;
        }
        
        User user = userOpt.get();
        LocalDate today = LocalDate.now();
        
        LocalDate lastXpDate = user.getLastXpDate();
        if (lastXpDate == null || !lastXpDate.equals(today)) {
            user.setDailyXp(0);
            
            if (lastXpDate != null) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastXpDate, today);
                if (daysBetween == 1) {
                    user.setCurrentStreak(user.getCurrentStreak() + 1);
                } else if (daysBetween == 2 && user.getStreakFreezes() > 0) {
                    user.setStreakFreezes(user.getStreakFreezes() - 1);
                    user.setCurrentStreak(user.getCurrentStreak() + 1);
                } else {
                    user.setCurrentStreak(1);
                }
            } else {
                user.setCurrentStreak(1);
            }
            
            if (user.getCurrentStreak() > user.getLongestStreak()) {
                user.setLongestStreak(user.getCurrentStreak());
            }
            
            user.setLastXpDate(today);
        }
        
        if (user.getDailyXp() >= DAILY_XP_LIMIT) {
            return; // Daily limit reached
        }
        
        int availableXp = DAILY_XP_LIMIT - user.getDailyXp();
        int xpToGrant = Math.min(xpAmount, availableXp);
        
        if (xpToGrant <= 0) return;
        
        int currentXp = user.getXp() != null ? user.getXp() : 0;
        int newXp = currentXp + xpToGrant;
        
        user.setXp(newXp);
        user.setDailyXp(user.getDailyXp() + xpToGrant);
        
        // Calculate level: Level 1 starts at 0 XP, each level requires 500 XP
        int newLevel = (newXp / 500) + 1;
        user.setLevel(newLevel);
        
        userRepository.save(user);
        
        XpHistory history = XpHistory.builder()
                .userId(userId)
                .xpAmount(xpToGrant)
                .source(source)
                .description(description)
                .build();
        xpHistoryRepository.save(history);
        
        badgeService.evaluateBadges(user);
    }
}
