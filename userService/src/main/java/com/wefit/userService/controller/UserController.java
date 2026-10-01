package com.wefit.userService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wefit.userService.dto.UserRequestDto;
import com.wefit.userService.dto.UserResponseDto;
import com.wefit.userService.service.UserService;

import org.springframework.cache.annotation.Cacheable;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.registerUser(userRequestDto));
    }

    @GetMapping("/keycloak/{keycloakId}")
    @Cacheable(value = "users", key = "#keycloakId")
    public ResponseEntity<UserResponseDto> getUserByKeycloakId(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.getUserByKeycloakId(keycloakId));
    }

    @GetMapping("/profile/{identifier}")
    @Cacheable(value = "users", key = "#identifier")
    public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable String identifier) {
        return ResponseEntity.ok(userService.getUserProfile(identifier));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "users", key = "#id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
