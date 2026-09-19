package com.wefit.aiService.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiProcessingException.class)
    public ProblemDetail handleAiProcessingException(AiProcessingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problemDetail.setType(URI.create("https://api.wefit.com/errors/ai-processing-error"));
        problemDetail.setTitle("AI Processing Error");
        addCorrelationId(problemDetail, ex);
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields.");
        problemDetail.setType(URI.create("https://api.wefit.com/errors/validation-failed"));
        problemDetail.setTitle("Validation Failed");
        
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("invalidFields", errors);
        addCorrelationId(problemDetail, ex);
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problemDetail.setType(URI.create("https://api.wefit.com/errors/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");
        addCorrelationId(problemDetail, ex);
        return problemDetail;
    }

    private void addCorrelationId(ProblemDetail problemDetail, Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        problemDetail.setProperty("correlationId", correlationId);
        if (problemDetail.getStatus() >= 500) {
            log.error("Exception correlationId: {} - Message: {}", correlationId, ex.getMessage(), ex);
        } else {
            log.warn("Exception correlationId: {} - Message: {}", correlationId, ex.getMessage());
        }
    }
}
