package com.wefit.userService.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "xp_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer xpAmount;
    
    @Column(nullable = false)
    private String source; // e.g., "ACTIVITY", "SOCIAL", "BONUS"
    
    private String description;
    
    @CreationTimestamp
    private LocalDateTime createdDateTime;
}
