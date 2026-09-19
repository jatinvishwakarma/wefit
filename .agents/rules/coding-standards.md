# WeFit — Coding Standards

## Language & Framework Conventions

- **Java 21** — Use modern Java features (records, pattern matching, etc.) where appropriate
- **Spring Boot 4.0.6** — Follow Spring Boot conventions for auto-configuration, component scanning
- **Spring Cloud 2025.1.1** — For service discovery and cloud-native patterns

## Package Structure

Every service follows this package layout under `com.wefit.<serviceName>`:

```
com.wefit.<serviceName>/
├── <ServiceName>Application.java    # Main class with @SpringBootApplication
├── config/                          # @Configuration classes
├── controller/                      # @RestController classes
├── dto/                             # Request/Response DTOs
├── entities/                        # @Entity (JPA) or @Document (MongoDB) classes
├── repositories/ or repository/     # Spring Data repository interfaces
└── service/                         # @Service business logic classes
```

> **Note**: Package naming is inconsistent: `activityService` and `aiService` use `repositories` (plural), while `userService` uses `repository` (singular). New code should use `repository` (singular) to align with Spring conventions.

## Lombok Usage

All services use Lombok extensively. Standard annotations on entities and DTOs:

```java
@Data                // Getters, setters, toString, equals, hashCode
@Builder             // Builder pattern
@NoArgsConstructor   // Required by JPA/MongoDB deserializers
@AllArgsConstructor  // Required by @Builder
```

For services with constructor injection:
```java
@AllArgsConstructor  // Used in UserService, ActivityService
@RequiredArgsConstructor  // Used in AiService (preferred — only injects final fields)
```

**Preferred pattern for new code**: Use `@RequiredArgsConstructor` with `private final` fields for dependency injection.

## Dependency Injection

Constructor injection via Lombok — **never use `@Autowired`**.

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository myRepository;  // Injected via constructor
}
```

## Entity Mapping Patterns

### DTO ↔ Entity Conversion

The project uses **static factory methods** on entity/DTO classes for conversion:

```java
// Entity → DTO (static method on DTO or Entity)
public static UserResponseDto toDto(User user) { ... }

// DTO → Entity (static method on Entity, confusingly named "fromEntity")
public static User fromEntity(UserRequestDto dto) { ... }
```

> **Note**: The naming `fromEntity` is misleading — it actually converts FROM a DTO TO an Entity. Consider renaming to `fromDto()` in future refactors.

> **Note**: Both `User.java` and `UserResponseDto.java` contain `toDto()` methods. Use the service-layer `convertToResponseDto()` private method pattern (as in `UserService.java`) for consistency.

## Controller Patterns

```java
@RestController
@RequestMapping("/api/<resource>")
@AllArgsConstructor  // or @RequiredArgsConstructor
public class MyController {

    private final MyService myService;

    @PostMapping("/action")
    public ResponseEntity<MyResponseDto> doSomething(@RequestBody MyRequestDto dto) {
        return ResponseEntity.ok(myService.doSomething(dto));
    }
}
```

- Always return `ResponseEntity<T>`
- Use `ResponseEntity.ok()` for success responses
- No custom error responses yet (tech debt)

## Error Handling

Currently uses **unchecked `RuntimeException`** with message strings:

```java
throw new RuntimeException("Email already exists");
throw new RuntimeException("User not found with id: " + id);
throw new RuntimeException("Invalid user");
```

**This is tech debt.** Future implementations should:
- Create custom exception classes (e.g., `UserNotFoundException`, `DuplicateEmailException`)
- Add a `@ControllerAdvice` global exception handler
- Return proper HTTP status codes (404, 409, etc.)

## Naming Conventions

| Element      | Convention          | Example                          |
|-------------|---------------------|----------------------------------|
| Service      | camelCase           | `userService`, `activityService` |
| Package      | camelCase           | `com.wefit.userService`          |
| Class        | PascalCase          | `ActivityService`                |
| Method       | camelCase           | `getUserProfile`                 |
| Endpoint     | kebab-case or camel | `/api/user/auth/register`        |
| Kafka Topic  | kebab-case          | `activity-events`                |
| Mongo Coll.  | PascalCase/snake    | `Activities`, `ai_recommendations` |
| DB Name      | PascalCamelCase     | `WefitActivitydb`                |

## Configuration Style

- YAML (`application.yml`) — not `.properties`
- No Spring profiles configured yet (no dev/prod split)
- No externalized configuration (no Config Server, no env vars)

## Code Quality Rules

1. **No `@Autowired` field injection** — always use constructor injection via Lombok
2. **Separate DTOs from entities** — never expose entity classes directly in controllers
3. **Use Lombok `@Builder`** — for constructing entity/DTO instances
4. **Static factory methods** for DTO conversion — not MapStruct or ModelMapper
5. **`@Slf4j`** for logging (used in AiService, adopt across all services)
