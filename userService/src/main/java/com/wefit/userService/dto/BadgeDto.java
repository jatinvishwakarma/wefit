package com.wefit.userService.dto;

import com.wefit.userService.entities.Badge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String tier;
    private String iconUrl;
    private LocalDateTime earnedDate;
    
    public static BadgeDto fromEntity(Badge badge, LocalDateTime earnedDate) {
        return BadgeDto.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .category(badge.getCategory())
                .tier(badge.getTier())
                .iconUrl(badge.getIconUrl())
                .earnedDate(earnedDate)
                .build();
    }
}
