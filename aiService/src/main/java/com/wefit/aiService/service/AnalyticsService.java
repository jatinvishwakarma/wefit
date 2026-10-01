package com.wefit.aiService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.aiService.dto.AnalyticsReportRequest;
import com.wefit.aiService.dto.AnalyticsReportResponse;
import com.wefit.aiService.entities.AnalyticsReport;
import com.wefit.aiService.repositories.AnalyticsReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final GeminiService geminiService;
    private final AnalyticsReportRepository analyticsReportRepository;
    private final SafetyEvaluationService safetyEvaluationService;
    private final ObjectMapper objectMapper;

    public AnalyticsReportResponse generateAnalyticsReport(Long userId, AnalyticsReportRequest request) {
        String prompt = createPromptForAnalytics(request);

        try {
            String aiResponse = geminiService.getRecommendations(prompt);
            log.info("AI Response for Analytics: {}", aiResponse);
            
            if (!safetyEvaluationService.isOutputSafe(aiResponse)) {
                log.warn("Unsafe AI output detected for analytics generation for user: {}", userId);
                throw new IllegalStateException("Generated analytics flagged by safety filters.");
            }
            
            AnalyticsReport report = parseAiResponseToReport(userId, request, aiResponse);
            AnalyticsReport savedReport = analyticsReportRepository.save(report);
            
            return mapToResponse(savedReport);
            
        } catch (Exception e) {
            log.error("Failed to generate analytics report", e);
            throw new RuntimeException("Failed to generate analytics report: " + e.getMessage());
        }
    }

    public List<AnalyticsReportResponse> getUserReports(Long userId) {
        return analyticsReportRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private String createPromptForAnalytics(AnalyticsReportRequest request) {
        return String.format("""
            Generate a progress analytics and burnout risk report in EXACT JSON format based on the following user data:
            Total Activities: %d
            Total Duration: %d minutes
            Total Calories Burned: %d kcal
            Days Since Last Rest: %d
            Average Heart Rate: %.1f bpm
            Goal: %s
            
            The JSON MUST have the following structure:
            {
              "burnoutRiskScore": 7.5,
              "burnoutWarning": "High risk of overtraining due to lack of rest days. Consider taking a rest day.",
              "progressAnalysis": "You are making great progress towards your goal.",
              "predictiveInsights": "At this rate, you will achieve your target within 4 weeks."
            }
            Ensure it is valid JSON and no markdown formatting wrappers are returned, just the raw JSON object starting with {.
            """, 
            request.getTotalActivities(), 
            request.getTotalDurationMinutes(), 
            request.getTotalCaloriesBurned(), 
            request.getDaysSinceLastRest(),
            request.getAverageHeartRate(),
            request.getGoal());
    }

    private AnalyticsReport parseAiResponseToReport(Long userId, AnalyticsReportRequest request, String aiResponse) throws Exception {
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

        return AnalyticsReport.builder()
                .userId(userId)
                .totalActivities(request.getTotalActivities())
                .totalDurationMinutes(request.getTotalDurationMinutes())
                .totalCaloriesBurned(request.getTotalCaloriesBurned())
                .burnoutRiskScore(rootNode.path("burnoutRiskScore").asDouble())
                .burnoutWarning(rootNode.path("burnoutWarning").asText())
                .progressAnalysis(rootNode.path("progressAnalysis").asText())
                .predictiveInsights(rootNode.path("predictiveInsights").asText())
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    private AnalyticsReportResponse mapToResponse(AnalyticsReport report) {
        return AnalyticsReportResponse.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .totalActivities(report.getTotalActivities())
                .totalDurationMinutes(report.getTotalDurationMinutes())
                .totalCaloriesBurned(report.getTotalCaloriesBurned())
                .burnoutRiskScore(report.getBurnoutRiskScore())
                .burnoutWarning(report.getBurnoutWarning())
                .progressAnalysis(report.getProgressAnalysis())
                .predictiveInsights(report.getPredictiveInsights())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
