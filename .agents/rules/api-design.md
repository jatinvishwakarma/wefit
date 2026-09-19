# Skill: API Design

## REST API Conventions in WeFit

### URL Structure
```
/api/<resource-plural>/<action-or-id>
```

Examples from the codebase:
- `/api/user/auth/register` — Auth-related user endpoints
- `/api/user/auth/{userId}/validate` — User validation
- `/api/users/profile/{identifier}` — User profile by identifier
- `/api/users/{id}` — User by ID
- `/api/activities/add` — Add activity
- `api/recommendations/user/{userId}` — User recommendations
- `api/recommendations/activity/{activityId}` — Activity recommendation

### HTTP Methods

| Operation | HTTP Method | Example |
|-----------|-------------|---------|
| Create    | POST        | `POST /api/activities/add` |
| Read one  | GET         | `GET /api/users/{id}` |
| Read list | GET         | `GET api/recommendations/user/{userId}` |
| Validate  | GET         | `GET /api/user/auth/{userId}/validate` |

> **Note**: Currently no PUT, PATCH, or DELETE endpoints exist.

### Request/Response Pattern

**Always use DTOs** — never expose entity classes directly in controllers.

> **Current exception**: `RecommendationController` returns `Recommendation` entity directly. This should be refactored to use a `RecommendationResponseDto`.

### Controller Template
```java
@RestController
@RequestMapping("/api/<resource>")
@RequiredArgsConstructor
public class MyController {
    private final MyService myService;

    @PostMapping("/create")
    public ResponseEntity<MyResponseDto> create(@Valid @RequestBody MyRequestDto dto) {
        return ResponseEntity.ok(myService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(myService.getById(id));
    }
}
```

### Request DTO Template
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email is not valid")
    private String email;

    private String optionalField;  // No validation = optional
}
```

### Response DTO Template
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyResponseDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdDateTime;

    public static MyResponseDto toDto(MyEntity entity) {
        return MyResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .createdDateTime(entity.getCreatedDateTime())
                .build();
    }
}
```

### Validation Annotations Used

| Annotation | Import | Usage |
|-----------|--------|-------|
| `@NotBlank` | `jakarta.validation.constraints` | Required String fields |
| `@Email` | `jakarta.validation.constraints` | Email format validation |
| `@Valid` | `jakarta.validation` | On `@RequestBody` parameter |

### Error Response Pattern (Current — needs improvement)

Currently, all errors are raw `RuntimeException` causing `500 Internal Server Error` with Spring's default error body. **This should be improved** with:

```java
// Future pattern: Custom exception
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}

// Future pattern: Global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### Checklist for Adding a New Endpoint

1. [ ] Define request DTO with validation annotations
2. [ ] Define response DTO with `toDto()` static factory
3. [ ] Add service method
4. [ ] Add controller method returning `ResponseEntity<T>`
5. [ ] Use `@Valid @RequestBody` for POST/PUT/PATCH
6. [ ] Use `@PathVariable` for path parameters
7. [ ] Always use leading `/` in `@RequestMapping` paths
8. [ ] Update `api-reference.md` documentation
