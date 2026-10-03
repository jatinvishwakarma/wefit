package com.wefit.aiService.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
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

import com.wefit.aiService.dto.CursorPageResponse;
import com.wefit.aiService.dto.RecommendationResponseDto;
import com.wefit.aiService.service.RecommendationService;

@WebMvcTest(RecommendationController.class)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    private RecommendationResponseDto recommendationDto;

    @BeforeEach
    void setUp() {
        recommendationDto = new RecommendationResponseDto();
        recommendationDto.setId("rec-123");
        recommendationDto.setUserId(1L);
        recommendationDto.setActivityId("activity-123");
        recommendationDto.setRecommendation("Good job running!");
    }

    @Test
    void getUserRecommendations_ReturnsList() throws Exception {
        CursorPageResponse<RecommendationResponseDto> pageResponse = new CursorPageResponse<>(List.of(recommendationDto), null, false);
        when(recommendationService.getUserRecommendations(anyLong(), isNull(), anyInt())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/recommendations/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("rec-123"))
                .andExpect(jsonPath("$.data[0].recommendation").value("Good job running!"));
    }

    @Test
    void getActivityRecommendation_ReturnsRecommendation() throws Exception {
        when(recommendationService.getActivityRecommendation(anyString())).thenReturn(recommendationDto);

        mockMvc.perform(get("/api/v1/recommendations/activity/activity-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rec-123"))
                .andExpect(jsonPath("$.recommendation").value("Good job running!"));
    }
}

