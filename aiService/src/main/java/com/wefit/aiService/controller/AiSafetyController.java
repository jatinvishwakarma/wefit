package com.wefit.aiService.controller;

import com.wefit.aiService.service.SafetyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/safety")
@RequiredArgsConstructor
public class AiSafetyController {

    private final SafetyEvaluationService safetyEvaluationService;

    @PostMapping("/evaluate-prompt")
    public ResponseEntity<Map<String, Object>> evaluatePrompt(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        boolean isSafe = safetyEvaluationService.isPromptSafe(prompt);
        if (!isSafe) {
            return ResponseEntity.ok(Map.of(
                    "isSafe", false,
                    "reason", "Prompt contains prohibited medical or harmful topics",
                    "fallbackMessage", safetyEvaluationService.getUnsafePromptFallbackMessage()
            ));
        }
        return ResponseEntity.ok(Map.of("isSafe", true));
    }
}
