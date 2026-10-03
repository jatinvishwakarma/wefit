package com.wefit.gamificationService.controller;

import com.wefit.gamificationService.dto.UserLevelResponse;
import com.wefit.gamificationService.service.UserLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class UserLevelController {

    private final UserLevelService userLevelService;

    @GetMapping("/level")
    public ResponseEntity<UserLevelResponse> getUserLevel(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(userLevelService.getUserLevel(userId));
    }

    @PostMapping("/streak/update")
    public ResponseEntity<Void> updateStreak(@RequestHeader("X-User-Id") UUID userId) {
        userLevelService.updateStreak(userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/xp/add")
    public ResponseEntity<Void> addXp(
            @RequestHeader("X-User-Id") UUID userId, 
            @RequestParam Long amount) {
        userLevelService.addXp(userId, amount);
        return ResponseEntity.ok().build();
    }
}
