package com.wefit.activityService.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.activityService.dto.ActivityRequestDto;
import com.wefit.activityService.dto.ActivityResponseDto;
import com.wefit.activityService.service.ActivityService;

import java.time.LocalDateTime;

@WebMvcTest(ActivityController.class)
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    private ActivityRequestDto activityRequestDto;
    private ActivityResponseDto activityResponseDto;

    @BeforeEach
    void setUp() {
        activityRequestDto = new ActivityRequestDto();
        activityRequestDto.setUserId(1L);
        activityRequestDto.setActivityType(com.wefit.activityService.entities.ActivityType.RUNNING);
        activityRequestDto.setDurationInMinutes(30);

        activityResponseDto = new ActivityResponseDto();
        activityResponseDto.setId("activity-123");
        activityResponseDto.setUserId(1L);
        activityResponseDto.setActivityType(com.wefit.activityService.entities.ActivityType.RUNNING);
        activityResponseDto.setDurationInMinutes(30);
        activityResponseDto.setStartTime(LocalDateTime.now());
    }

    @Test
    void addActivity_ReturnsSavedActivity() throws Exception {
        when(activityService.addActivity(any(ActivityRequestDto.class))).thenReturn(activityResponseDto);

        mockMvc.perform(post("/api/v1/activities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("activity-123"))
                .andExpect(jsonPath("$.activityType").value("RUNNING"));
    }
}
