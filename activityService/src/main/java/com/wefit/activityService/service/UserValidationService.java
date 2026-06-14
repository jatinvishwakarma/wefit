package com.wefit.activityService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser(Long userId) {
        try {
            Boolean isValid = userServiceWebClient.get()
                    .uri("/api/user/auth/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return isValid != null && isValid;
        } catch (Exception e) {
            return false;
        }
    }
}
