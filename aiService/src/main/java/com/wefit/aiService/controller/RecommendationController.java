package com.wefit.aiService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wefit.aiService.dto.CursorPageResponse;
import com.wefit.aiService.dto.RecommendationResponseDto;
import com.wefit.aiService.service.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("user/{userId}")
    public ResponseEntity<CursorPageResponse<RecommendationResponseDto>> getUserRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") int limit){
        return ResponseEntity.ok(recommendationService.getUserRecommendations(userId, cursor, limit));
    }

    @GetMapping("activity/{activityId}")
    public ResponseEntity<RecommendationResponseDto> getActivityRecommendation(@PathVariable String activityId){
        return ResponseEntity.ok(recommendationService.getActivityRecommendation(activityId));
    }
    
    @org.springframework.web.bind.annotation.PostMapping("/{recommendationId}/rate")
    public ResponseEntity<Void> rateRecommendation(
            @PathVariable String recommendationId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> request) {
        Integer rating = (Integer) request.get("rating");
        String feedback = (String) request.get("feedback");
        recommendationService.rateRecommendation(recommendationId, rating, feedback);
        return ResponseEntity.ok().build();
    }
}
