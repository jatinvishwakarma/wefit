package com.wefit.userService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.wefit.userService.dto.UserRequestDto;
import com.wefit.userService.dto.UserResponseDto;
import com.wefit.userService.entities.User;
import com.wefit.userService.exception.UserConflictException;
import com.wefit.userService.exception.UserNotFoundException;
import com.wefit.userService.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequestDto userRequestDto;
    private User user;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test@test.com");
        userRequestDto.setFirstName("Test");
        userRequestDto.setLastName("User");
        userRequestDto.setUserName("testuser");
        userRequestDto.setPassword("Password123!");
        userRequestDto.setKeycloakId("keycloak-id-123");

        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .userName("testuser")
                .keycloakId("keycloak-id-123")
                .build();
    }

    @Test
    void registerUser_NewUser_ReturnsResponseDto() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto responseDto = userService.registerUser(userRequestDto);

        assertNotNull(responseDto);
        assertEquals("test@test.com", responseDto.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ExistingUserNoKeycloakId_LinksKeycloakId() {
        User existingUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .keycloakId(null)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto responseDto = userService.registerUser(userRequestDto);

        assertNotNull(responseDto);
        verify(userRepository).save(existingUser);
    }

    @Test
    void registerUser_ExistingUserDifferentKeycloakId_ThrowsException() {
        User existingUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .keycloakId("different-keycloak-id")
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        assertThrows(UserConflictException.class, () -> userService.registerUser(userRequestDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserProfile_UserExists_ReturnsUser() {
        when(userRepository.findByUserNameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        UserResponseDto responseDto = userService.getUserProfile("testuser");

        assertNotNull(responseDto);
        assertEquals("testuser", responseDto.getUserName());
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsException() {
        when(userRepository.findByUserNameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserProfile("nonexistent"));
    }
}
