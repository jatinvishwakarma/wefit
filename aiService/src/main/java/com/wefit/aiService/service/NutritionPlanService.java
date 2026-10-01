package com.wefit.aiService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.aiService.dto.NutritionPlanRequest;
import com.wefit.aiService.dto.NutritionPlanResponse;
import com.wefit.aiService.entities.NutritionPlan;
import com.wefit.aiService.repositories.NutritionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionPlanService {

    private final GeminiService geminiService;
    private final NutritionPlanRepository nutritionPlanRepository;
    private final SafetyEvaluationService safetyEvaluationService;
    private final ObjectMapper objectMapper;

    public NutritionPlanResponse generatePlan(Long userId, NutritionPlanRequest request) {
        
        double bmr = calculateBmr(request);
        double tdee = calculateTdee(bmr, request.getActivityLevel());
        double targetCalories = calculateTargetCalories(tdee, request.getGoal());
        
        // 30% Protein, 40% Carbs, 30% Fat roughly
        double proteinGrams = (targetCalories * 0.3) / 4;
        double carbsGrams = (targetCalories * 0.4) / 4;
        double fatGrams = (targetCalories * 0.3) / 9;

        String prompt = createPromptForPlan(request, targetCalories, proteinGrams, carbsGrams, fatGrams);
        
        if (!safetyEvaluationService.isPromptSafe(prompt)) {
            log.warn("Unsafe prompt detected for nutrition plan generation for user: {}", userId);
            throw new IllegalArgumentException("Your request contains unsafe or medically sensitive topics.");
        }

        try {
            String aiResponse = geminiService.getRecommendations(prompt);
            log.info("AI Response for Nutrition Plan: {}", aiResponse);
            
            if (!safetyEvaluationService.isOutputSafe(aiResponse)) {
                log.warn("Unsafe AI output detected for nutrition plan generation for user: {}", userId);
                throw new IllegalStateException("Generated nutrition plan flagged by safety filters.");
            }
            
            NutritionPlan plan = parseAiResponseToPlan(userId, request, targetCalories, proteinGrams, carbsGrams, fatGrams, aiResponse);
            NutritionPlan savedPlan = nutritionPlanRepository.save(plan);
            
            return mapToResponse(savedPlan);
            
        } catch (Exception e) {
            log.error("Failed to generate nutrition plan", e);
            throw new RuntimeException("Failed to generate nutrition plan: " + e.getMessage());
        }
    }

    public List<NutritionPlanResponse> getUserPlans(Long userId) {
        return nutritionPlanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    private double calculateBmr(NutritionPlanRequest request) {
        // Mifflin-St Jeor Equation
        if ("MALE".equalsIgnoreCase(request.getGender())) {
            return (10 * request.getWeightKg()) + (6.25 * request.getHeightCm()) - (5 * request.getAge()) + 5;
        } else {
            return (10 * request.getWeightKg()) + (6.25 * request.getHeightCm()) - (5 * request.getAge()) - 161;
        }
    }
    
    private double calculateTdee(double bmr, String activityLevel) {
        return switch (activityLevel != null ? activityLevel.toUpperCase() : "SEDENTARY") {
            case "LIGHTLY_ACTIVE" -> bmr * 1.375;
            case "MODERATELY_ACTIVE" -> bmr * 1.55;
            case "VERY_ACTIVE" -> bmr * 1.725;
            case "EXTRA_ACTIVE" -> bmr * 1.9;
            default -> bmr * 1.2; // SEDENTARY
        };
    }
    
    private double calculateTargetCalories(double tdee, String goal) {
        return switch (goal != null ? goal.toUpperCase() : "MAINTAIN") {
            case "LOSE_WEIGHT" -> tdee - 500;
            case "GAIN_WEIGHT" -> tdee + 500;
            default -> tdee;
        };
    }

    private String createPromptForPlan(NutritionPlanRequest request, double targetCalories, double protein, double carbs, double fat) {
        return String.format("""
            Generate meal suggestions for a day in EXACT JSON format based on the following:
            Target Calories: %.0f kcal
            Target Macros: Protein %.0fg, Carbs %.0fg, Fat %.0fg
            Dietary Preferences: %s
            
            The JSON MUST have the following structure:
            {
              "mealSuggestions": [
                {
                  "mealType": "Breakfast",
                  "description": "Oatmeal with protein powder and berries",
                  "calories": 450,
                  "macros": "35g Protein, 50g Carbs, 10g Fat"
                }
              ]
            }
            Provide exactly 4 meal suggestions (Breakfast, Lunch, Dinner, Snack).
            Ensure it is valid JSON and no markdown formatting wrappers are returned, just the raw JSON object starting with {.
            Remember to add a disclaimer in notes that this is informational and not medical advice.
            """, 
            targetCalories, protein, carbs, fat,
            request.getDietaryPreferences() != null ? request.getDietaryPreferences() : "None");
    }

    private NutritionPlan parseAiResponseToPlan(Long userId, NutritionPlanRequest request, double targetCalories, double protein, double carbs, double fat, String aiResponse) throws Exception {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(aiResponse);
        } catch (Exception e) {
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

        List<NutritionPlan.MealSuggestion> mealSuggestions = new ArrayList<>();
        if (rootNode.has("mealSuggestions") && rootNode.get("mealSuggestions").isArray()) {
            for (JsonNode mealNode : rootNode.get("mealSuggestions")) {
                mealSuggestions.add(NutritionPlan.MealSuggestion.builder()
                        .mealType(mealNode.path("mealType").asText())
                        .description(mealNode.path("description").asText())
                        .calories(mealNode.path("calories").asInt())
                        .macros(mealNode.path("macros").asText())
                        .build());
            }
        }

        return NutritionPlan.builder()
                .userId(userId)
                .age(request.getAge())
                .gender(request.getGender())
                .weightKg(request.getWeightKg())
                .heightCm(request.getHeightCm())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .targetCalories(targetCalories)
                .proteinGrams(protein)
                .carbsGrams(carbs)
                .fatGrams(fat)
                .mealSuggestions(mealSuggestions)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    private NutritionPlanResponse mapToResponse(NutritionPlan plan) {
        return NutritionPlanResponse.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .age(plan.getAge())
                .gender(plan.getGender())
                .weightKg(plan.getWeightKg())
                .heightCm(plan.getHeightCm())
                .activityLevel(plan.getActivityLevel())
                .goal(plan.getGoal())
                .targetCalories(plan.getTargetCalories())
                .proteinGrams(plan.getProteinGrams())
                .carbsGrams(plan.getCarbsGrams())
                .fatGrams(plan.getFatGrams())
                .mealSuggestions(plan.getMealSuggestions())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
