package com.wefit.aiService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.wefit.aiService.dto.CursorPageResponse;
import com.wefit.aiService.dto.RecommendationResponseDto;
import com.wefit.aiService.entities.Recommendation;
import com.wefit.aiService.repositories.RecommendationRepository;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Recommendation recommendation;

    @BeforeEach
    void setUp() {
        recommendation = new Recommendation();
        recommendation.setId("rec-123");
        recommendation.setUserId(1L);
        recommendation.setActivityId("activity-123");
        recommendation.setRecommendation("Good job running!");
    }

    @Test
    void getUserRecommendations_ReturnsList() {
        List<Recommendation> recs = new ArrayList<>();
        recs.add(recommendation);
        
        when(recommendationRepository.findByUserIdOrderByIdDesc(anyLong(), any(Pageable.class))).thenReturn(recs);

        CursorPageResponse<RecommendationResponseDto> result = recommendationService.getUserRecommendations(1L, null, 5);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("rec-123", result.getData().get(0).getId());
    }

    @Test
    void getActivityRecommendation_ReturnsRecommendation() {
        when(recommendationRepository.findByActivityId(anyString())).thenReturn(recommendation);

        RecommendationResponseDto result = recommendationService.getActivityRecommendation("activity-123");

        assertNotNull(result);
        assertEquals("rec-123", result.getId());
    }
}

