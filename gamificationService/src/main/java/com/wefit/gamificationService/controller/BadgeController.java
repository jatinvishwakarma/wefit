package com.wefit.gamificationService.controller;

import com.wefit.gamificationService.dto.BadgeResponse;
import com.wefit.gamificationService.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<List<BadgeResponse>> getUserBadges(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }

    @PostMapping("/{badgeId}/award")
    public ResponseEntity<Void> awardBadge(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID badgeId) {
        badgeService.awardBadge(userId, badgeId);
        return ResponseEntity.ok().build();
    }
}
