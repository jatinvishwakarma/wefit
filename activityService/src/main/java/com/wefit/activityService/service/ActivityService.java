package com.wefit.activityService.service;

import org.springframework.stereotype.Service;

import com.wefit.activityService.dto.ActivityRequestDto;
import com.wefit.activityService.dto.ActivityResponseDto;
import com.wefit.activityService.entities.Activity;
import com.wefit.activityService.repositories.ActivityRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    public ActivityResponseDto addActivity(ActivityRequestDto activityRequestDto) {
        Activity activity = Activity.fromEntity(activityRequestDto);
        Activity savedActivity = activityRepository.save(activity);
        return ActivityResponseDto.toDto(savedActivity);
    }
}
