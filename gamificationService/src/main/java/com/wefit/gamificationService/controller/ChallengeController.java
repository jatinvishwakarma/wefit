package com.wefit.gamificationService.controller;

import com.wefit.gamificationService.dto.ChallengeRequest;
import com.wefit.gamificationService.dto.ChallengeResponse;
import com.wefit.gamificationService.service.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(@Valid @RequestBody ChallengeRequest request) {
        return ResponseEntity.ok(challengeService.createChallenge(request));
    }

    @GetMapping
    public ResponseEntity<Page<ChallengeResponse>> getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(challengeService.getChallenges(PageRequest.of(page, size)));
    }

    @PostMapping("/{challengeId}/join")
    public ResponseEntity<Void> joinChallenge(
            @PathVariable UUID challengeId,
            @RequestHeader("X-User-Id") UUID userId) {
        challengeService.joinChallenge(challengeId, userId);
        return ResponseEntity.ok().build();
    }
}
