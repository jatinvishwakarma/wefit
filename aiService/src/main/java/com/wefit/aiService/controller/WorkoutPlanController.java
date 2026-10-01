package com.wefit.aiService.controller;

import com.wefit.aiService.dto.WorkoutPlanRequest;
import com.wefit.aiService.dto.WorkoutPlanResponse;
import com.wefit.aiService.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/workout-plans")
@RequiredArgsConstructor
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping("/{userId}/generate")
    public ResponseEntity<WorkoutPlanResponse> generatePlan(
            @PathVariable Long userId,
            @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(workoutPlanService.generatePlan(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<WorkoutPlanResponse>> getUserPlans(@PathVariable Long userId) {
        return ResponseEntity.ok(workoutPlanService.getUserPlans(userId));
    }
}
