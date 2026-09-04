package com.wefit.userService.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.wefit.userService.dto.UserRequestDto;
import com.wefit.userService.dto.UserResponseDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keycloakId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    private String bio;

    private String gender;

    private String dateOfBirth;

    private String profilePicUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    private LocalDateTime updadatedDateTime;

    public static User fromEntity(UserRequestDto userRequestDto) {
        return User.builder()
                .keycloakId(userRequestDto.getKeycloakId())
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .userName(userRequestDto.getUserName())
                .email(userRequestDto.getEmail())
                .password(userRequestDto.getPassword())
                .phoneNumber(userRequestDto.getPhoneNumber())
                .bio(userRequestDto.getBio())
                .gender(userRequestDto.getGender())
                .dateOfBirth(userRequestDto.getDateOfBirth())
                .role(userRequestDto.getRole())
                .build();
    }

    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .profilePicUrl(user.getProfilePicUrl())
                .createdDateTime(user.getCreatedDateTime())
                .updadatedDateTime(user.getUpdadatedDateTime())
                .build();
    }
}
