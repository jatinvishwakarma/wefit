package com.wefit.aiService.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.wefit.aiService.entities.Recommendation;
import com.wefit.aiService.service.RecommendationService;

@WebMvcTest(RecommendationController.class)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    private Recommendation recommendation;

    @BeforeEach
    void setUp() {
        recommendation = new Recommendation();
        recommendation.setId("rec-123");
        recommendation.setUserId(1L);
        recommendation.setActivityId("activity-123");
        recommendation.setRecommendationText("Good job running!");
    }

    @Test
    void getUserRecommendations_ReturnsList() throws Exception {
        when(recommendationService.getUserRecommendations(anyLong())).thenReturn(List.of(recommendation));

        mockMvc.perform(get("/api/recommendations/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rec-123"))
                .andExpect(jsonPath("$[0].recommendationText").value("Good job running!"));
    }

    @Test
    void getActivityRecommendation_ReturnsRecommendation() throws Exception {
        when(recommendationService.getActivityRecommendation(anyString())).thenReturn(recommendation);

        mockMvc.perform(get("/api/recommendations/activity/activity-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rec-123"))
                .andExpect(jsonPath("$.recommendationText").value("Good job running!"));
    }
}
