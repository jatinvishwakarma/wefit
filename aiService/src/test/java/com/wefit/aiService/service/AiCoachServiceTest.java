package com.wefit.aiService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.aiService.dto.ChatRequest;
import com.wefit.aiService.dto.ChatResponse;
import com.wefit.aiService.entities.ChatSession;
import com.wefit.aiService.repositories.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiCoachServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private SafetyEvaluationService safetyEvaluationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiCoachService aiCoachService;

    @Test
    void chat_UnsafePrompt_ReturnsFallback() {
        when(safetyEvaluationService.isPromptSafe("bad prompt")).thenReturn(false);
        when(safetyEvaluationService.getUnsafePromptFallbackMessage()).thenReturn("Fallback");

        ChatRequest request = new ChatRequest();
        request.setMessage("bad prompt");

        ChatResponse response = aiCoachService.chat(1L, request);

        assertFalse(response.isSafe());
        assertEquals("Fallback", response.getResponse());
        verify(geminiService, never()).getChatResponse(any());
    }

    @Test
    void chat_SafePrompt_HappyPath() throws Exception {
        when(safetyEvaluationService.isPromptSafe("good prompt")).thenReturn(true);
        when(safetyEvaluationService.isOutputSafe("AI reply")).thenReturn(true);
        when(geminiService.getChatResponse(any())).thenReturn("raw json");

        ChatSession session = ChatSession.builder().userId(1L).build();
        when(chatSessionRepository.findByUserId(1L)).thenReturn(Optional.of(session));

        JsonNode mockRootNode = mock(JsonNode.class);
        JsonNode mockCandidates = mock(JsonNode.class);
        JsonNode mockCandidate = mock(JsonNode.class);
        JsonNode mockContent = mock(JsonNode.class);
        JsonNode mockParts = mock(JsonNode.class);
        JsonNode mockPart = mock(JsonNode.class);
        JsonNode mockText = mock(JsonNode.class);

        when(objectMapper.readTree("raw json")).thenReturn(mockRootNode);
        when(mockRootNode.path("candidates")).thenReturn(mockCandidates);
        when(mockCandidates.get(0)).thenReturn(mockCandidate);
        when(mockCandidate.path("content")).thenReturn(mockContent);
        when(mockContent.path("parts")).thenReturn(mockParts);
        when(mockParts.get(0)).thenReturn(mockPart);
        when(mockPart.path("text")).thenReturn(mockText);
        when(mockText.asText()).thenReturn("AI reply");

        ChatRequest request = new ChatRequest();
        request.setMessage("good prompt");

        ChatResponse response = aiCoachService.chat(1L, request);

        assertTrue(response.isSafe());
        assertEquals("AI reply", response.getResponse());
        verify(chatSessionRepository, times(1)).save(session);
        assertEquals(2, session.getMessages().size());
    }
}
