package com.wefit.aiService.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class SafetyEvaluationServiceTest {

    @InjectMocks
    private SafetyEvaluationService safetyEvaluationService;

    @Test
    void isPromptSafe_SafePrompt() {
        assertTrue(safetyEvaluationService.isPromptSafe("How do I do a push up?"));
    }

    @Test
    void isPromptSafe_UnsafePrompt() {
        assertFalse(safetyEvaluationService.isPromptSafe("My knee has pain"));
        assertFalse(safetyEvaluationService.isPromptSafe("I want to harm myself"));
        assertFalse(safetyEvaluationService.isPromptSafe("Can you diagnose my disease?"));
    }

    @Test
    void isOutputSafe_SafeOutput() {
        assertTrue(safetyEvaluationService.isOutputSafe("You should do 3 sets of 10 pushups."));
    }

    @Test
    void isOutputSafe_UnsafeOutput() {
        assertFalse(safetyEvaluationService.isOutputSafe("I will prescribe you some medicine."));
    }
    
    @Test
    void fallbackMessage_isCorrect() {
        String msg = safetyEvaluationService.getUnsafePromptFallbackMessage();
        assertTrue(msg.contains("medical professional"));
    }
}
