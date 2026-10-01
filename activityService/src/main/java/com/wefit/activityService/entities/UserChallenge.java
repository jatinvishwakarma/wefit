package com.wefit.activityService.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_challenges")
public class UserChallenge {
    @Id
    private String id;
    
    private Long userId;
    private String challengeId;
    
    @Builder.Default
    private Double progressValue = 0.0;
    
    @Builder.Default
    private Boolean isCompleted = false;
    
    private LocalDateTime completedAt;
    
    @CreatedDate
    private LocalDateTime joinedAt;
}
