# Skill: Spring Boot Patterns

> Last updated: 2026-06-13 — reflects working production patterns for WeFit microservices

## Project-Specific Spring Boot Configuration

### Versions
- Spring Boot: **4.0.6**
- Spring Cloud: **2025.1.1**
- Java: **21**

### Spring Boot Application Class
```java
package com.wefit.<serviceName>;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class <ServiceName>Application {
    public static void main(String[] args) {
        SpringApplication.run(<ServiceName>Application.class, args);
    }
}
```

For Eureka Server, add `@EnableEurekaServer`.
For Kafka consumers, add `@EnableKafka` on the main application class.

### application.yml Template
```yaml
spring:
  application:
    name: <serviceName>

server:
  port: <port>

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

## Dependency Injection Pattern

**Always use constructor injection via Lombok:**
```java
@Service
@RequiredArgsConstructor  // Preferred over @AllArgsConstructor
public class MyService {
    private final MyRepository myRepository;   // final = injected
    private final OtherService otherService;   // final = injected
}
```

**Never use:**
```java
@Autowired  // ❌ Field injection
private MyRepository myRepository;
```

## Controller Pattern
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

## Entity Patterns

### JPA Entity (PostgreSQL)
```java
@Entity
@Table(name = "my_table")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MyEnum status = MyEnum.DEFAULT;

    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    private LocalDateTime updatedDateTime;
}
```

### MongoDB Document
```java
@Document("collection_name")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyDocument {
    @Id
    private String id;

    @Field("custom_field_name")  // Optional field rename
    private Map<String, Object> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**MongoDB auditing requires** `@EnableMongoAuditing` configuration:
```java
@Configuration
@EnableMongoAuditing
public class MongoConfigurations {}
```

## Repository Patterns

### JPA Repository
```java
@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    Optional<MyEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### MongoDB Repository
```java
@Repository
public interface MyRepository extends MongoRepository<MyDocument, String> {
    List<MyDocument> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    MyDocument findByActivityId(String activityId);
}
```

## DTO Conversion Pattern
```java
// On the ResponseDto class
public static MyResponseDto toDto(MyEntity entity) {
    return MyResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .build();
}
```

## Configuration Beans

### WebClient for Inter-Service Calls (Direct URL — no Eureka)

> ⚠️ **Do NOT use `@LoadBalanced`** unless Eureka is actively running and all services are registered.
> Using `@LoadBalanced` without Eureka causes silent DNS failures and `Invalid user` 500 errors.

```java
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

### RestClient for External API Calls (e.g. Gemini)

> ⚠️ **Do NOT use `WebClient` (Netty) for external HTTPS calls** — it causes `NotSslRecordException`
> on Windows/JDK21. Use `RestClient` (Spring Boot 4's synchronous HTTP client) instead.

```java
@Configuration
public class WebClientConfig {
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
```

```java
@Service
public class GeminiService {
    private final RestClient restClient;

    public GeminiService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String call(String url, String apiKey, Object body) {
        return restClient.post()
            .uri(url + "?key=" + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);
    }
}
```

## Kafka Configuration

### Producer (application.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    properties:
      spring.json.add.type.headers: false

kafka:
  topic:
    name: my-topic
```

### Consumer (application.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: my-consumer-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    properties:
      spring.json.add.type.headers: false
      spring.json.value.default.type: com.wefit.myService.entities.MyEntity
      spring.json.trusted.packages: "*"
```
