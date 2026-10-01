package com.wefit.aiService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.aiService.dto.ChatRequest;
import com.wefit.aiService.dto.ChatResponse;
import com.wefit.aiService.entities.ChatSession;
import com.wefit.aiService.repositories.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiCoachService {

    private final GeminiService geminiService;
    private final ChatSessionRepository chatSessionRepository;
    private final SafetyEvaluationService safetyEvaluationService;
    private final ObjectMapper objectMapper;

    public ChatResponse chat(Long userId, ChatRequest request) {
        String userMessage = request.getMessage();

        if (!safetyEvaluationService.isPromptSafe(userMessage)) {
            log.warn("Unsafe chat prompt detected for user: {}", userId);
            return ChatResponse.builder()
                    .response(safetyEvaluationService.getUnsafePromptFallbackMessage())
                    .isSafe(false)
                    .build();
        }

        ChatSession session = chatSessionRepository.findByUserId(userId)
                .orElse(ChatSession.builder().userId(userId).build());

        session.getMessages().add(ChatSession.ChatMessage.builder()
                .role("user")
                .content(userMessage)
                .timestamp(LocalDateTime.now())
                .build());

        try {
            List<Map<String, Object>> contents = buildGeminiContents(session.getMessages());
            String aiResponseRaw = geminiService.getChatResponse(contents);
            String aiResponseText = extractTextFromGeminiResponse(aiResponseRaw);

            if (!safetyEvaluationService.isOutputSafe(aiResponseText)) {
                log.warn("Unsafe AI output detected for user: {}", userId);
                aiResponseText = "I'm sorry, I cannot provide a response to that due to safety guidelines.";
            }

            session.getMessages().add(ChatSession.ChatMessage.builder()
                    .role("model")
                    .content(aiResponseText)
                    .timestamp(LocalDateTime.now())
                    .build());
            
            chatSessionRepository.save(session);

            return ChatResponse.builder()
                    .response(aiResponseText)
                    .isSafe(true)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate chat response via Gemini", e);
            return ChatResponse.builder()
                    .response("I'm currently unable to process your request. Please try again later.")
                    .isSafe(true)
                    .build();
        }
    }

    private List<Map<String, Object>> buildGeminiContents(List<ChatSession.ChatMessage> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();
        // Only use the last 10 messages for context to limit token size
        int start = Math.max(0, messages.size() - 10);
        for (int i = start; i < messages.size(); i++) {
            ChatSession.ChatMessage msg = messages.get(i);
            Map<String, Object> content = new HashMap<>();
            content.put("role", msg.getRole());
            content.put("parts", new Object[]{Map.of("text", msg.getContent())});
            contents.add(content);
        }
        return contents;
    }

    private String extractTextFromGeminiResponse(String aiResponse) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(aiResponse);
        return jsonNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text").asText();
    }
}
