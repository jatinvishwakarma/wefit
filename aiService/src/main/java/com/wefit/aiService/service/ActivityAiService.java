package com.wefit.aiService.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.aiService.entities.Activity;
import com.wefit.aiService.entities.Recommendation;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import com.wefit.aiService.repositories.RecommendationRepository;

@Service
@Slf4j
@AllArgsConstructor
public class ActivityAiService {

    private final GeminiService geminiService;
    private final RecommendationRepository recommendationRepository;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        try {
            String aiResponse = geminiService.getRecommendations(prompt);
            log.info("RESPONSE FROM AI- {}", aiResponse);
            return processAiResponse(aiResponse, activity);
        } catch (Exception e) {
            log.error("Failed to generate recommendation via Gemini", e);
            Recommendation rec = Recommendation.builder()
                .userId(activity.getUserId())
                .activityId(activity.getId())
                .recommendation("Fallback Recommendation: We couldn't connect to the AI service, but keep up the good work!")
                .createdAt(java.time.LocalDateTime.now())
                .build();
            return recommendationRepository.save(rec);
        }
    }

    private Recommendation processAiResponse(String aiResponse, Activity activity) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            JsonNode jsonNode2 = jsonNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonString = jsonNode2.asText()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .replaceAll("\\n", "")
                    .trim();

            JsonNode finalJson = objectMapper.readTree(jsonString);
            log.info("FINAL JSON : {}", finalJson);
            
            java.util.List<String> improvementsList = new java.util.ArrayList<>();
            if (finalJson.has("improvements") && finalJson.get("improvements").isArray()) {
                for (JsonNode node : finalJson.get("improvements")) {
                    improvementsList.add(node.path("area").asText() + ": " + node.path("recommendation").asText());
                }
            }
            
            java.util.List<String> suggestionsList = new java.util.ArrayList<>();
            if (finalJson.has("suggestions") && finalJson.get("suggestions").isArray()) {
                for (JsonNode node : finalJson.get("suggestions")) {
                    suggestionsList.add(node.path("workout").asText() + ": " + node.path("description").asText());
                }
            }
            
            java.util.List<String> safetyList = new java.util.ArrayList<>();
            if (finalJson.has("safety") && finalJson.get("safety").isArray()) {
                for (JsonNode node : finalJson.get("safety")) {
                    safetyList.add(node.asText());
                }
            }
            
            String overallAnalysis = finalJson.has("analysis") && finalJson.get("analysis").has("overall") 
                    ? finalJson.get("analysis").get("overall").asText() : "Analysis provided by AI";
                    
            Recommendation rec = Recommendation.builder()
                .userId(activity.getUserId())
                .activityId(activity.getId())
                .recommendation(overallAnalysis)
                .improvements(improvementsList)
                .suggestions(suggestionsList)
                .safetyPrecautions(safetyList)
                .createdAt(java.time.LocalDateTime.now())
                .build();
                
            return recommendationRepository.save(rec);
            
        } catch (Exception e) {
            log.error("Error processing AI response", e);
        }

        return null;
    }

    private String createPromptForActivity(Activity activity) {
        return String.format(
                """
                        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
                        {
                          "analysis": {
                            "overall": "Overall analysis here",
                            "pace": "Pace analysis here",
                            "heartRate": "Heart rate analysis here",
                            "caloriesBurned": "Calories analysis here"
                          },
                          "improvements": [
                            {
                              "area": "Area name",
                              "recommendation": "Detailed recommendation"
                            }
                          ],
                          "suggestions": [
                            {
                              "workout": "Workout name",
                              "description": "Detailed workout description"
                            }
                          ],
                          "safety": [
                            "Safety point 1",
                            "Safety point 2"
                          ]
                        }

                        Analyze this activity:
                        Activity Type: %s
                        Duration: %d minutes
                        Calories Burned: %d
                        Additional Metrics: %s

                        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
                        Ensure the response follows the EXACT JSON format shown above.
                        """,
                activity.getActivityType(),
                activity.getDurationInMinutes(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics());
    }

}
