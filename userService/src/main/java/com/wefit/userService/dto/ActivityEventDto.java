package com.wefit.userService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityEventDto {
    private Long id;
    private Long userId;
    private String activityType;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private LocalDateTime activityDate;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
}
