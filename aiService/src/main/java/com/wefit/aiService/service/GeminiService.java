package com.wefit.aiService.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GeminiService {

    private final RestClient restClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public GeminiService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String getRecommendations(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                });

        return callGemini(requestBody);
    }
    
    public String getChatResponse(java.util.List<Map<String, Object>> contents) {
        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "systemInstruction", Map.of("parts", new Object[]{
                        Map.of("text", "You are an AI Personal Coach for the Wefit app. You provide encouraging, safe, and helpful fitness advice.")
                })
        );
        return callGemini(requestBody);
    }
    
    private String callGemini(Map<String, Object> requestBody) {
        log.info("Calling Gemini API at: {}", geminiApiUrl);

        return restClient
                .post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }
}
