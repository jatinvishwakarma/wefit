package com.wefit.aiService.controller;

import com.wefit.aiService.dto.NutritionPlanRequest;
import com.wefit.aiService.dto.NutritionPlanResponse;
import com.wefit.aiService.service.NutritionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/nutrition-plans")
@RequiredArgsConstructor
public class NutritionPlanController {

    private final NutritionPlanService nutritionPlanService;

    @PostMapping("/{userId}/generate")
    public ResponseEntity<NutritionPlanResponse> generatePlan(
            @PathVariable Long userId,
            @RequestBody NutritionPlanRequest request) {
        return ResponseEntity.ok(nutritionPlanService.generatePlan(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NutritionPlanResponse>> getUserPlans(@PathVariable Long userId) {
        return ResponseEntity.ok(nutritionPlanService.getUserPlans(userId));
    }
}
