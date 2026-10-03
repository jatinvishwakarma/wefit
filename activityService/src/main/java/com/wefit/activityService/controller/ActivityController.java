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
@RequestMapping("/api/v1/activities")
@AllArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @io.swagger.v3.oas.annotations.Operation(summary = "Add a new activity", description = "Creates a new fitness activity record for a user.")
    @PostMapping("/add")
    public ResponseEntity<ActivityResponseDto> addActivity(@jakarta.validation.Valid @RequestBody ActivityRequestDto activityRequestDto) {
        ActivityResponseDto savedActivity = activityService.addActivity(activityRequestDto);
        return ResponseEntity.ok(savedActivity);
    }

}
