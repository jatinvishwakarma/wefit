package com.wefit.activityService.service;

import org.springframework.stereotype.Service;

import com.wefit.activityService.dto.ActivityRequestDto;
import com.wefit.activityService.dto.ActivityResponseDto;
import com.wefit.activityService.entities.Activity;
import com.wefit.activityService.repositories.ActivityRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public ActivityService(ActivityRepository activityRepository, UserValidationService userValidationService,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        this.activityRepository = activityRepository;
        this.userValidationService = userValidationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public ActivityResponseDto addActivity(ActivityRequestDto activityRequestDto) {
        boolean isValidUser = userValidationService.validateUser(activityRequestDto.getUserId());
        if (!isValidUser) {
            throw new RuntimeException("Invalid user");
        }
        Activity activity = Activity.fromEntity(activityRequestDto);
        Activity savedActivity = activityRepository.save(activity);

        kafkaTemplate.send(topicName, savedActivity);

        return ActivityResponseDto.toDto(savedActivity);
    }
}
