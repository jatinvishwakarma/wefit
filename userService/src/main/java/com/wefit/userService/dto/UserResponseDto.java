package com.wefit.userService.dto;

import java.time.LocalDateTime;

import com.wefit.userService.entities.User;
import com.wefit.userService.entities.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    private String bio;
    private String gender;
    private String dateOfBirth;
    private UserRole role;
    private String profilePicUrl;
    private LocalDateTime createdDateTime;
    private LocalDateTime updadatedDateTime;

    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
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
