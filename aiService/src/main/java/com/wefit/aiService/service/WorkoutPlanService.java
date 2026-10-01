package com.wefit.aiService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.aiService.dto.WorkoutPlanRequest;
import com.wefit.aiService.dto.WorkoutPlanResponse;
import com.wefit.aiService.entities.WorkoutPlan;
import com.wefit.aiService.repositories.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutPlanService {

    private final GeminiService geminiService;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final SafetyEvaluationService safetyEvaluationService;
    private final ObjectMapper objectMapper;

    public WorkoutPlanResponse generatePlan(Long userId, WorkoutPlanRequest request) {
        String prompt = createPromptForPlan(request);
        
        if (!safetyEvaluationService.isPromptSafe(prompt)) {
            log.warn("Unsafe prompt detected for workout plan generation for user: {}", userId);
            throw new IllegalArgumentException("Your request contains unsafe or medically sensitive topics.");
        }

        try {
            String aiResponse = geminiService.getRecommendations(prompt);
            log.info("AI Response for Workout Plan: {}", aiResponse);
            
            if (!safetyEvaluationService.isOutputSafe(aiResponse)) {
                log.warn("Unsafe AI output detected for workout plan generation for user: {}", userId);
                throw new IllegalStateException("Generated workout plan flagged by safety filters.");
            }
            
            WorkoutPlan plan = parseAiResponseToPlan(userId, request, aiResponse);
            WorkoutPlan savedPlan = workoutPlanRepository.save(plan);
            
            return mapToResponse(savedPlan);
            
        } catch (Exception e) {
            log.error("Failed to generate workout plan", e);
            throw new RuntimeException("Failed to generate workout plan: " + e.getMessage());
        }
    }

    public List<WorkoutPlanResponse> getUserPlans(Long userId) {
        return workoutPlanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String createPromptForPlan(WorkoutPlanRequest request) {
        return String.format("""
            Generate a personalized workout plan in EXACT JSON format.
            The user's goal is %s, fitness level is %s, and they want to workout %d days per week.
            Additional preferences: %s.
            
            The JSON MUST have the following structure:
            {
              "schedule": [
                {
                  "day": 1,
                  "focus": "Upper Body",
                  "notes": "Warm up properly",
                  "exercises": [
                    {
                      "name": "Push ups",
                      "sets": "3",
                      "reps": "10-15",
                      "rest": "60s"
                    }
                  ]
                }
              ]
            }
            Generate exactly %d days of workouts in the schedule array. Ensure it is valid JSON and no markdown formatting wrappers are returned, just the raw JSON object starting with {.
            """, 
            request.getGoal(), 
            request.getFitnessLevel(), 
            request.getDaysPerWeek(), 
            request.getAdditionalPreferences() != null ? request.getAdditionalPreferences() : "None",
            request.getDaysPerWeek());
    }

    private WorkoutPlan parseAiResponseToPlan(Long userId, WorkoutPlanRequest request, String aiResponse) throws Exception {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(aiResponse);
        } catch (Exception e) {
            // Try to extract from Gemini's markdown wrapper
            JsonNode geminiNode = objectMapper.readTree(aiResponse);
            String jsonString = geminiNode
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            rootNode = objectMapper.readTree(jsonString);
        }

        List<WorkoutPlan.DailyWorkout> schedule = new ArrayList<>();
        if (rootNode.has("schedule") && rootNode.get("schedule").isArray()) {
            for (JsonNode dayNode : rootNode.get("schedule")) {
                List<WorkoutPlan.Exercise> exercises = new ArrayList<>();
                if (dayNode.has("exercises") && dayNode.get("exercises").isArray()) {
                    for (JsonNode exNode : dayNode.get("exercises")) {
                        exercises.add(WorkoutPlan.Exercise.builder()
                                .name(exNode.path("name").asText())
                                .sets(exNode.path("sets").asText())
                                .reps(exNode.path("reps").asText())
                                .rest(exNode.path("rest").asText())
                                .build());
                    }
                }
                
                schedule.add(WorkoutPlan.DailyWorkout.builder()
                        .day(dayNode.path("day").asInt())
                        .focus(dayNode.path("focus").asText())
                        .notes(dayNode.path("notes").asText())
                        .exercises(exercises)
                        .build());
            }
        }

        return WorkoutPlan.builder()
                .userId(userId)
                .goal(request.getGoal())
                .fitnessLevel(request.getFitnessLevel())
                .daysPerWeek(request.getDaysPerWeek())
                .schedule(schedule)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    private WorkoutPlanResponse mapToResponse(WorkoutPlan plan) {
        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .goal(plan.getGoal())
                .fitnessLevel(plan.getFitnessLevel())
                .daysPerWeek(plan.getDaysPerWeek())
                .schedule(plan.getSchedule())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
