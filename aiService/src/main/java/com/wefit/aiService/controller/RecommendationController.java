package com.wefit.aiService.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wefit.aiService.entities.Recommendation;
import com.wefit.aiService.service.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("user/{userId}")
    public ResponseEntity<List<Recommendation>> getUserRecommendations(@PathVariable Long userId){
        return ResponseEntity.ok(recommendationService.getUserRecommendations(userId));
    }

    @GetMapping("activity/{activityId}")
    public ResponseEntity<Recommendation>getActivityRecommendation(@PathVariable String activityId){
        return ResponseEntity.ok(recommendationService.getActivityRecommendation(activityId));
    }


}
