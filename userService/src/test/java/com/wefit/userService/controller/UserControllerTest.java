package com.wefit.userService.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wefit.userService.dto.UserRequestDto;
import com.wefit.userService.dto.UserResponseDto;
import com.wefit.userService.service.UserService;

import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
public class UserControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public org.springframework.cache.CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;



    @Autowired
    private ObjectMapper objectMapper;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("test@test.com");
        userRequestDto.setFirstName("Test");
        userRequestDto.setLastName("User");
        userRequestDto.setUserName("testuser");
        userRequestDto.setPassword("Password123!");
        userRequestDto.setKeycloakId("keycloak-id-123");

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .userName("testuser")
                .keycloakId("keycloak-id-123")
                .build();
    }

    @Test
    void registerUser_ReturnsCreatedUser() throws Exception {
        when(userService.registerUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getUserByKeycloakId_ReturnsUser() throws Exception {
        when(userService.getUserByKeycloakId(anyString())).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/v1/users/keycloak/keycloak-id-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value("keycloak-id-123"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getUserProfile_ReturnsUser() throws Exception {
        when(userService.getUserProfile(anyString())).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/v1/users/profile/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }
}
