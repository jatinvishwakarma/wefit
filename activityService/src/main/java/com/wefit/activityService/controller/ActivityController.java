package com.wefit.activityService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wefit.activityService.dto.ActivityRequestDto;
import com.wefit.activityService.dto.ActivityResponseDto;
import com.wefit.activityService.service.ActivityService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/add")
    public ResponseEntity<ActivityResponseDto> addActivity(@RequestBody ActivityRequestDto activityRequestDto) {
        ActivityResponseDto savedActivity = activityService.addActivity(activityRequestDto);
        return ResponseEntity.ok(savedActivity);
    }

}
