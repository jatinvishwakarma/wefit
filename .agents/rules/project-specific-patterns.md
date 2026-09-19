# Skill: Project-Specific Patterns

> Last updated: 2026-06-13 — reflects working production patterns for WeFit microservices

## Patterns Unique to WeFit

### 1. Multi-Database Microservice Pattern (Polyglot Persistence)

WeFit uses different databases for different services based on data characteristics:
- **Relational data** (users, roles) → PostgreSQL with JPA
- **Document data** (activities, recommendations) → MongoDB

When adding a new service, choose the database based on:
- Structured, relational data with constraints → PostgreSQL + JPA
- Flexible schema, nested objects, rapid iteration → MongoDB

### 2. Cross-Service User Validation Pattern

Before any operation that references a user, validate via REST call:

```java
// Pattern used in ActivityService → UserService
boolean isValid = userValidationService.validateUser(userId);
if (!isValid) {
    throw new RuntimeException("Invalid user");
}
```

The validation call is wrapped in try/catch — any error (service down, network timeout) returns `false`. This is a **fail-closed** pattern (deny on uncertainty).

**Key file**: [UserValidationService.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/service/UserValidationService.java)

### 3. Event-Driven Processing Pattern (Kafka)

When a business action should trigger async processing in another service:

```
Service A: Save to DB → Publish to Kafka topic
Service B: Kafka consumer → Process → Save result
```

**Kafka topic naming**: kebab-case (e.g., `activity-events`)
**Consumer group naming**: descriptive (e.g., `activity-processor-group`)

### 4. Static Factory Method DTO Conversion

```java
// On Response DTO
public static MyResponseDto toDto(MyEntity entity) {
    return MyResponseDto.builder()
            .field1(entity.getField1())
            .field2(entity.getField2())
            .build();
}

// On Entity (converts FROM DTO, confusingly named "fromEntity")
public static MyEntity fromEntity(MyRequestDto dto) {
    return MyEntity.builder()
            .field1(dto.getField1())
            .build();
}
```

> **Preferred naming for future code**:
> - `toDto(Entity)` → on DTO class
> - `fromDto(Dto)` → on Entity class (rename from `fromEntity`)

### 5. WebClient for Inter-Service Calls (Direct URL — NO @LoadBalanced)

> ⚠️ **Known issue fixed**: The original `@LoadBalanced` + `http://userService` pattern
> caused `Invalid user` 500 errors when Eureka wasn't running. Now uses direct URLs.

```java
// activityService/config/WebClientConfig.java
@Configuration
public class WebClientConfig {
    @Value("${user-service.base-url:http://localhost:8081}")
    private String userServiceBaseUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient userServiceWebClient() {
        return webClientBuilder().baseUrl(userServiceBaseUrl).build();
    }
}
```

### 6. RestClient for External API Calls (Gemini AI)

> ⚠️ **Use `RestClient`, NOT `WebClient`** for external HTTPS APIs.
> `WebClient` (Netty) causes `NotSslRecordException` on Windows/JDK21.

```java
// aiService/config/WebClientConfig.java
@Configuration
public class WebClientConfig {
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}

// GeminiService.java
@Service
public class GeminiService {
    private final RestClient restClient;
    @Value("${gemini.api.url}") private String geminiApiUrl;
    @Value("${gemini.api.key}") private String geminiApiKey;

    public GeminiService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String getRecommendations(String prompt) {
        return restClient.post()
            .uri(geminiApiUrl + "?key=" + geminiApiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("contents", new Object[]{ Map.of("parts", new Object[]{ Map.of("text", prompt) }) }))
            .retrieve()
            .body(String.class);
    }
}
```

**application.yml** (aiService):
```yaml
gemini:
  api:
    url: ${GEMINI_URL:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}
    key: ${GEMINI_API_KEY:}
```

For auto-populating `createdAt` and `updatedAt` fields:

1. Add `@EnableMongoAuditing` configuration class
2. Use `@CreatedDate` and `@LastModifiedDate` annotations on entity fields

### 8. Builder.Default for Enum Defaults

```java
@Enumerated(EnumType.STRING)
@Builder.Default
private UserRole role = UserRole.USER;
```

Without `@Builder.Default`, the builder would set the field to `null` even if a default is specified in the field declaration.

### 9. Kafka JSON Serialization Without Type Headers

✅ This pattern is **working** in WeFit (ActivityService → AiService).
```yaml
spring.json.add.type.headers: false
```

The consumer specifies the default type explicitly:
```yaml
spring.json.value.default.type: com.wefit.aiService.entities.Activity
```


### 10. Gemini AI Fallback Pattern

Always wrap external AI calls in a try/catch. Save a fallback recommendation on failure:

```java
public Recommendation generateRecommendation(Activity activity) {
    try {
        String aiResponse = geminiService.getRecommendations(prompt);
        return processAiResponse(aiResponse, activity); // parse JSON, save
    } catch (Exception e) {
        log.error("Gemini API failed, using fallback", e);
        return recommendationRepository.save(
            Recommendation.builder()
                .userId(activity.getUserId())
                .activityId(activity.getId())
                .recommendation("Fallback: AI service temporarily unavailable.")
                .createdAt(LocalDateTime.now())
                .build()
        );
    }
}
```

### 11. Environment Variable Loading Pattern

All secrets live in `.env` at the project root. Services read them via Spring's `${VAR_NAME:default}` syntax.

```
# .env (never commit to git)
GEMINI_API_KEY=AIza...
DB_PASSWORD=...
MONGODB_URI_AI_SERVICE=mongodb://localhost:27017/AiRecommendationsdb
```

To start a service with `.env` loaded (PowerShell):
```powershell
Get-Content c:\Wefit\.env | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}
cd c:\Wefit\aiService && ./mvnw.cmd spring-boot:run
```

> ⚠️ `start-services.bat` uses `cmd /k` with new console windows that do **not** inherit
> env vars from the parent. Use `start_services.py` (which passes `env=os.environ`) or
> load `.env` manually before running each service.

---

## Service Naming Conventions

| Context | Convention | Example |
|---------|-----------|---------|
| Maven artifactId | camelCase | `activityService` |
| Spring app name | camelCase | `activityService` |
| Eureka service name | camelCase (auto from spring.application.name) | `activityService` |
| WebClient base URL | kebab-case (Eureka is case-insensitive) | `http://user-service` |
| Package name | camelCase | `com.wefit.activityService` |
| Kafka topic | kebab-case | `activity-events` |
| MongoDB database | PascalCase + "db" suffix | `WefitActivitydb` |
| MongoDB collection | PascalCase or snake_case | `Activities`, `ai_recommendations` |
| PostgreSQL database | lowercase | `wefit` |
| PostgreSQL table | lowercase plural | `users` |

---

## File Location Patterns

When creating a new file, follow these location rules:

| File Type | Location |
|-----------|----------|
| Main application class | `src/main/java/com/wefit/<service>/` |
| Configuration class | `src/main/java/com/wefit/<service>/config/` |
| REST controller | `src/main/java/com/wefit/<service>/controller/` |
| Request/Response DTO | `src/main/java/com/wefit/<service>/dto/` |
| Entity / Document | `src/main/java/com/wefit/<service>/entities/` |
| Enum | `src/main/java/com/wefit/<service>/entities/` |
| Repository interface | `src/main/java/com/wefit/<service>/repository/` |
| Service class | `src/main/java/com/wefit/<service>/service/` |
| Kafka listener | `src/main/java/com/wefit/<service>/service/` |
| Application config | `src/main/resources/application.yml` |
| Tests | `src/test/java/com/wefit/<service>/` |
