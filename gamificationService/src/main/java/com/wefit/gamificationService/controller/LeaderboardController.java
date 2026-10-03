package com.wefit.gamificationService.controller;

import com.wefit.gamificationService.dto.UserLevelResponse;
import com.wefit.gamificationService.model.UserLevel;
import com.wefit.gamificationService.repository.UserLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final UserLevelRepository userLevelRepository;

    @GetMapping("/global")
    public ResponseEntity<List<UserLevelResponse>> getGlobalLeaderboard() {
        List<UserLevelResponse> leaderboard = userLevelRepository.findTop10ByOrderByXpDesc()
                .stream()
                .map(userLevel -> UserLevelResponse.builder()
                        .userId(userLevel.getUserId())
                        .xp(userLevel.getXp())
                        .level(userLevel.getLevel())
                        .currentStreak(userLevel.getCurrentStreak())
                        .longestStreak(userLevel.getLongestStreak())
                        .lastActivityDate(userLevel.getLastActivityDate())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }
}
