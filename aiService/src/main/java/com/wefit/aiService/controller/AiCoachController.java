package com.wefit.aiService.controller;

import com.wefit.aiService.dto.ChatRequest;
import com.wefit.aiService.dto.ChatResponse;
import com.wefit.aiService.service.AiCoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/coach")
@RequiredArgsConstructor
public class AiCoachController {

    private final AiCoachService aiCoachService;

    @PostMapping("/{userId}/chat")
    public ResponseEntity<ChatResponse> chat(
            @PathVariable Long userId,
            @RequestBody ChatRequest request) {
        return ResponseEntity.ok(aiCoachService.chat(userId, request));
    }
}
