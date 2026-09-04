package com.wefit.userService.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.wefit.userService.dto.UserRequestDto;
import com.wefit.userService.dto.UserResponseDto;
import com.wefit.userService.entities.User;
import com.wefit.userService.entities.UserRole;
import com.wefit.userService.exception.ResourceNotFoundException;
import com.wefit.userService.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        // If a user with this email already exists, link the Keycloak account to it
        Optional<User> existingUserOpt = userRepository.findByEmail(userRequestDto.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // Link if keycloakId is not yet set
            if (existingUser.getKeycloakId() == null || existingUser.getKeycloakId().isEmpty()) {
                existingUser.setKeycloakId(userRequestDto.getKeycloakId());
                existingUser = userRepository.save(existingUser);
                return convertToResponseDto(existingUser);
            }
            // If already linked to another keycloak account (unlikely but handled)
            if (!existingUser.getKeycloakId().equals(userRequestDto.getKeycloakId())) {
                 throw new RuntimeException("Email already linked to another Keycloak account");
            }
            return convertToResponseDto(existingUser);
        }

        User user = User.builder()
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
                .role(userRequestDto.getRole() != null ? userRequestDto.getRole() : UserRole.USER)
                .createdDateTime(LocalDateTime.now())
                .updadatedDateTime(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }

    public UserResponseDto getUserProfile(String identifier) {
        User user = userRepository.findByUserNameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username or email: " + identifier));
        return convertToResponseDto(user);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToResponseDto(user);
    }

    public UserResponseDto getUserByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloakId: " + keycloakId));
        return convertToResponseDto(user);
    }

    private UserResponseDto convertToResponseDto(User user) {
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

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}
