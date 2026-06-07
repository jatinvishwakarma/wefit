package com.wefit.userService.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.wefit.userService.dto.UserRequestDto;
import com.wefit.userService.dto.UserResponseDto;
import com.wefit.userService.entities.User;
import com.wefit.userService.entities.UserRole;
import com.wefit.userService.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .firstName(userRequestDto.getFirstName())
                .lastName(userRequestDto.getLastName())
                .userName(userRequestDto.getUserName())
                .email(userRequestDto.getEmail())
                .password(userRequestDto.getPassword())
                .phoneNumber(userRequestDto.getPhoneNumber())
                .bio(userRequestDto.getBio())
                .gender(userRequestDto.getGender())
                .dateOfBirth(userRequestDto.getDateOfBirth())
                .role(userRequestDto.getRole() != null ? userRequestDto.getRole() : UserRole.USER)
                .createdDateTime(LocalDateTime.now())
                .updadatedDateTime(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }

    public UserResponseDto getUserProfile(String identifier) {
        User user = userRepository.findByUserNameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found with username or email: " + identifier));
        return convertToResponseDto(user);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToResponseDto(user);
    }

    private UserResponseDto convertToResponseDto(User user) {
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
