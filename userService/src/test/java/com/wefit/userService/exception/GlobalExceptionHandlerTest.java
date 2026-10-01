package com.wefit.userService.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleUserNotFound_ShouldReturn404() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ProblemDetail response = exceptionHandler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("User not found", response.getDetail());
        assertEquals("User Not Found", response.getTitle());
        assertNotNull(response.getProperties().get("correlationId"));
    }

    @Test
    void handleUserConflict_ShouldReturn409() {
        UserConflictException ex = new UserConflictException("User already exists");
        ProblemDetail response = exceptionHandler.handleUserConflict(ex);

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals("User already exists", response.getDetail());
        assertEquals("User Conflict", response.getTitle());
        assertNotNull(response.getProperties().get("correlationId"));
    }

    @Test
    void handleValidationExceptions_ShouldReturn400AndInvalidFields() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("userDto", "email", "Invalid email format");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail response = exceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Validation Failed", response.getTitle());
        assertNotNull(response.getProperties().get("correlationId"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> invalidFields = (Map<String, String>) response.getProperties().get("invalidFields");
        assertNotNull(invalidFields);
        assertEquals("Invalid email format", invalidFields.get("email"));
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception ex = new Exception("Something went wrong");
        ProblemDetail response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("An unexpected error occurred.", response.getDetail());
        assertEquals("Internal Server Error", response.getTitle());
        assertNotNull(response.getProperties().get("correlationId"));
    }
}
