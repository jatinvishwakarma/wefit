package com.wefit.aiService.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SafetyEvaluationService {

    private static final List<String> MEDICAL_KEYWORDS = List.of(
            "pain", "injury", "diagnose", "treatment", "doctor", "medical", 
            "symptom", "disease", "broken", "fracture", "sprain", "torn"
    );

    private static final List<String> HARMFUL_KEYWORDS = List.of(
            "suicide", "harm", "kill", "die", "starve", "anorexia", "bulimia"
    );

    public boolean isPromptSafe(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return false;
        }

        String lowerPrompt = prompt.toLowerCase();
        
        for (String keyword : MEDICAL_KEYWORDS) {
            if (lowerPrompt.contains(keyword)) {
                return false;
            }
        }
        
        for (String keyword : HARMFUL_KEYWORDS) {
            if (lowerPrompt.contains(keyword)) {
                return false;
            }
        }

        return true;
    }
    
    public boolean isOutputSafe(String output) {
        if (output == null || output.isBlank()) {
            return false;
        }

        String lowerOutput = output.toLowerCase();
        
        // Block if output tries to diagnose or prescribe
        List<String> prescribeKeywords = List.of("prescribe", "diagnose", "treatment plan", "medical advice");
        for (String keyword : prescribeKeywords) {
            if (lowerOutput.contains(keyword)) {
                return false;
            }
        }
        
        for (String keyword : HARMFUL_KEYWORDS) {
            if (lowerOutput.contains(keyword)) {
                return false;
            }
        }

        return true;
    }
    
    public String getUnsafePromptFallbackMessage() {
        return "I am an AI fitness assistant, not a medical professional. I cannot provide medical advice, diagnose injuries, or suggest treatments. Please consult a qualified healthcare provider for these concerns.";
    }
}
