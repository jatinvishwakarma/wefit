# Skill: Debugging

> Last updated: 2026-06-13 — reflects known bugs found and fixed in WeFit microservices

## Common Debugging Scenarios

### Service Not Registering with Eureka

1. **Check Eureka is running**: Visit `http://localhost:8761`
2. **Check service name**: Application name in `application.yml` should match what Eureka shows
3. **Check Eureka URL**: `eureka.client.service-url.defaultZone` should be `http://localhost:8761/eureka`
4. **Check network**: Ensure no firewall blocking

### WebClient "Invalid user" 500 Error (Eureka Not Running)

**Symptom**: `POST /api/activities/add` returns 500 with `RuntimeException: Invalid user`
even when the user exists in the database.

**Root cause**: `WebClientConfig` uses `@LoadBalanced` and resolves `http://userService`
via Eureka. When Eureka is NOT running, the DNS resolution silently fails, `validateUser()`
catches the exception and returns `false`, which throws `Invalid user`.

**Fix** — use a direct URL instead of Eureka service name:
```java
// Remove @LoadBalanced, use direct URL with configurable base
@Value("${user-service.base-url:http://localhost:8081}")
private String userServiceBaseUrl;

@Bean
public WebClient userServiceWebClient() {
    return webClientBuilder().baseUrl(userServiceBaseUrl).build();
}
```

See [WebClientConfig.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/config/WebClientConfig.java)

### `NotSslRecordException` When Calling External HTTPS APIs (e.g. Gemini)

```
io.netty.handler.codec.DecoderException:
  io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record
```

**Root cause**: Spring Boot 4's `WebClient` uses **Netty** as its HTTP transport.
When used with `.block()` (blocking call) on Windows/JDK21, Netty's SSL handler
fails to negotiate TLS with external HTTPS endpoints.

**Fix** — use `RestClient` instead of `WebClient` for external API calls:
```java
// aiService/config/WebClientConfig.java
@Bean
public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
}

// GeminiService.java
private final RestClient restClient;
public GeminiService(RestClient.Builder builder) {
    this.restClient = builder.build();
}
// Use: restClient.post().uri(...).retrieve().body(String.class)
```

See [GeminiService.java](file:///c:/Wefit/aiService/src/main/java/com/wefit/aiService/service/GeminiService.java)

### Kafka Consumer Not Receiving Messages

**Debug steps**:
1. Verify Kafka broker is running: `kafka-topics --list --bootstrap-server localhost:9092`
2. Verify topic exists: `kafka-topics --describe --topic activity-events --bootstrap-server localhost:9092`
3. Check consumer group: `kafka-consumer-groups --describe --group activity-processor-group --bootstrap-server localhost:9092`
4. Check deserialization config in `application.yml`:
   - `spring.json.value.default.type` must match the consumer entity class FQCN
   - `spring.json.trusted.packages` must include the producer's entity package
5. **Critical**: Verify the producer is actually sending messages (Kafka producer publish is NOT yet implemented in ActivityService)

### MongoDB Connection Issues

```
com.mongodb.MongoSocketOpenException: Exception opening socket
```

1. Check MongoDB is running: `mongosh` or `mongo` command
2. Check connection URI in `application.yml`
3. Check MongoDB port (default 27017)
4. Check if authentication is required

### PostgreSQL Connection Issues

```
org.postgresql.util.PSQLException: Connection refused
```

1. Check PostgreSQL is running: `pg_isready -h localhost -p 5432`
2. Check database exists: `psql -U postgres -c "\l"` (look for `wefit`)
3. Check credentials match `application.yml`
4. Check `pg_hba.conf` for local connection rules

### Hibernate/JPA Issues

**"Table not found" after field rename**:
- `ddl-auto: update` adds columns but never drops them
- Fix: Drop and recreate the table, or use a migration tool

**"Could not determine type" for enum fields**:
- Ensure `@Enumerated(EnumType.STRING)` is present
- Without it, Hibernate tries to store enums as ordinal integers

### Lombok "Cannot find symbol" Compilation Errors

1. Enable annotation processing in your IDE:
   - **IntelliJ**: Settings → Build → Compiler → Annotation Processors → Enable
   - **VS Code**: Install "Lombok Annotations Support" extension
2. Verify `maven-compiler-plugin` has Lombok in `annotationProcessorPaths`
3. Run `mvnw.cmd clean compile` to rebuild

## Logging

### Adding Logging to a Class
```java
@Slf4j  // Lombok — creates `log` field
@Service
public class MyService {
    public void doSomething() {
        log.info("Processing request for user: {}", userId);
        log.debug("Detailed data: {}", data);
        log.error("Failed to process: {}", e.getMessage(), e);
    }
}
```

### Current Logging Usage
- `ActivityMessageListener` uses `@Slf4j` with `log.info()`
- Other services have no custom logging (should be added)

### Useful Log Levels for Debugging

Add to `application.yml`:
```yaml
logging:
  level:
    com.wefit: DEBUG
    org.springframework.web: DEBUG
    org.springframework.data: DEBUG
    org.apache.kafka: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # Shows SQL parameter values
```

## Port Conflict Resolution (Windows)

```powershell
# Find what's using a port
netstat -ano | findstr :<PORT>

# Kill the process
taskkill /PID <PID> /F
```

## Quick Health Checks (PowerShell)

```powershell
# Check service ports
8081, 8082, 8083 | ForEach-Object {
    $c = Get-NetTCPConnection -LocalPort $_ -ErrorAction SilentlyContinue
    if ($c) { "Port $_ - RUNNING" } else { "Port $_ - NOT RUNNING" }
}

# Validate a known user (replace 1 with real user ID)
Invoke-RestMethod http://localhost:8081/api/user/auth/1/validate

# Add a test activity (requires valid userId)
$body = @{ userId=1; activityType="RUNNING"; durationInMinutes=30; caloriesBurned=300; additionalMetrics=@{pace="5min/km"} } | ConvertTo-Json
Invoke-RestMethod http://localhost:8082/api/activities/add -Method Post -Body $body -ContentType "application/json"

# Fetch recommendations for a user
Invoke-RestMethod http://localhost:8083/api/recommendations/user/1
```

## Full E2E Test Script

```powershell
# Register user → Post activity → Wait → Fetch recommendation
$rand = Get-Random
$user = Invoke-RestMethod http://localhost:8081/api/user/auth/register -Method Post `
  -Body (@{firstName="Test";lastName="User";userName="t$rand";email="t$rand@t.com";password="pass123"} | ConvertTo-Json) `
  -ContentType "application/json"

$activity = Invoke-RestMethod http://localhost:8082/api/activities/add -Method Post `
  -Body (@{userId=$user.id;activityType="RUNNING";durationInMinutes=30;caloriesBurned=300;additionalMetrics=@{}} | ConvertTo-Json) `
  -ContentType "application/json"

Start-Sleep 20  # Wait for Kafka → aiService

Invoke-RestMethod "http://localhost:8083/api/recommendations/user/$($user.id)"
```
