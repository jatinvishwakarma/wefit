package com.wefit.activityService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.wefit.activityService.dto.ActivityRequestDto;
import com.wefit.activityService.dto.ActivityResponseDto;
import com.wefit.activityService.entities.Activity;
import com.wefit.activityService.exception.InvalidActivityException;
import com.wefit.activityService.repository.ActivityRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @InjectMocks
    private ActivityService activityService;

    private ActivityRequestDto activityRequestDto;
    private Activity activity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(activityService, "topicName", "test-topic");

        activityRequestDto = new ActivityRequestDto();
        activityRequestDto.setUserId(1L);
        activityRequestDto.setActivityType("RUNNING");
        activityRequestDto.setDurationMinutes(30);

        activity = new Activity();
        activity.setId("activity-123");
        activity.setUserId(1L);
        activity.setActivityType("RUNNING");
        activity.setDurationMinutes(30);
    }

    @Test
    void addActivity_ValidUser_SavesAndPublishesToKafka() {
        when(userValidationService.validateUser(anyLong())).thenReturn(true);
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ActivityResponseDto responseDto = activityService.addActivity(activityRequestDto);

        assertNotNull(responseDto);
        assertEquals("activity-123", responseDto.getId());
        assertEquals("RUNNING", responseDto.getActivityType());

        verify(activityRepository).save(any(Activity.class));
        verify(kafkaTemplate).send(anyString(), any(Activity.class));
    }

    @Test
    void addActivity_InvalidUser_ThrowsException() {
        when(userValidationService.validateUser(anyLong())).thenReturn(false);

        assertThrows(InvalidActivityException.class, () -> activityService.addActivity(activityRequestDto));

        verify(activityRepository, never()).save(any(Activity.class));
        verify(kafkaTemplate, never()).send(anyString(), any(Activity.class));
    }
}
