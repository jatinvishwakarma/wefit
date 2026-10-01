package com.wefit.userService.service;

import com.wefit.userService.dto.ActivityEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ActivityKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(ActivityKafkaListener.class);
    private final GamificationService gamificationService;

    public ActivityKafkaListener(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(topics = "${kafka.topic.activity-events}", groupId = "user-service-group")
    public void consumeActivityEvent(ActivityEventDto event) {
        log.info("Received activity event for user: {}", event.getUserId());
        
        // Calculate XP based on duration and calories
        // Base XP: 5 XP per minute + 1 XP per 10 calories
        int durationMinutes = event.getDurationMinutes() != null ? event.getDurationMinutes() : 0;
        int calories = event.getCaloriesBurned() != null ? event.getCaloriesBurned() : 0;
        
        int calculatedXp = (durationMinutes * 5) + (calories / 10);
        
        // Ensure at least some XP is granted if valid activity
        if (calculatedXp == 0 && (durationMinutes > 0 || calories > 0)) {
            calculatedXp = 10;
        }
        
        if (calculatedXp > 0 && event.getUserId() != null) {
            gamificationService.grantXp(event.getUserId(), calculatedXp, "ACTIVITY", "Completed an activity");
            log.info("Granted {} XP to user {}", calculatedXp, event.getUserId());
        }
    }
}
