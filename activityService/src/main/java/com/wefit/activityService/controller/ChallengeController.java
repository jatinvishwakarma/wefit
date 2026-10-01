package com.wefit.activityService.controller;

import com.wefit.activityService.dto.ChallengeRequest;
import com.wefit.activityService.dto.ChallengeResponse;
import com.wefit.activityService.dto.UserChallengeResponse;
import com.wefit.activityService.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    // ─── Admin endpoints ───────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(@RequestBody ChallengeRequest request) {
        return ResponseEntity.ok(challengeService.createChallenge(request));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ChallengeResponse>> getActiveChallenges() {
        return ResponseEntity.ok(challengeService.getActiveChallenges());
    }

    // ─── User endpoints ────────────────────────────────────────────────────────

    @PostMapping("/{challengeId}/join/{userId}")
    public ResponseEntity<UserChallengeResponse> joinChallenge(
            @PathVariable String challengeId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(challengeService.joinChallenge(userId, challengeId));
    }

    @PatchMapping("/{challengeId}/progress/{userId}")
    public ResponseEntity<UserChallengeResponse> updateProgress(
            @PathVariable String challengeId,
            @PathVariable Long userId,
            @RequestParam Double value) {
        return ResponseEntity.ok(challengeService.updateProgress(userId, challengeId, value));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserChallengeResponse>> getMyChallenges(@PathVariable Long userId) {
        return ResponseEntity.ok(challengeService.getMyChallenges(userId));
    }
}
