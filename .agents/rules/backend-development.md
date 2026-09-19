# Skill: Backend Development

## When Adding a New Microservice

1. **Create Maven project** with `spring-boot-starter-parent` 4.0.6
2. **Set properties**:
   ```xml
   <java.version>21</java.version>
   <spring-cloud.version>2025.1.1</spring-cloud.version>
   ```
3. **Add Eureka client** dependency:
   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```
4. **Configure application.yml**:
   ```yaml
   spring:
     application:
       name: <serviceName>
   server:
     port: <next available port>
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8761/eureka
   ```
5. **Follow the layered package structure**:
   ```
   com.wefit.<serviceName>/
   ├── config/
   ├── controller/
   ├── dto/
   ├── entities/
   ├── repository/
   └── service/
   ```

## When Adding a New Feature to an Existing Service

1. **Entity**: Create in `entities/` package with Lombok annotations (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
2. **Repository**: Create in `repository/` package extending `MongoRepository` or `JpaRepository`
3. **DTOs**: Create request/response DTOs in `dto/` with static `toDto()` methods
4. **Service**: Create in `service/` with `@Service` and `@RequiredArgsConstructor`
5. **Controller**: Create in `controller/` with `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`

## When Adding Inter-Service Communication

### Synchronous (WebClient)
```java
// 1. Add webflux dependency to pom.xml
// 2. Create WebClient config
@Configuration
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient targetServiceWebClient() {
        return webClientBuilder().baseUrl("http://<eureka-service-name>").build();
    }
}

// 3. Inject and use in service class
@Service
@RequiredArgsConstructor
public class MyService {
    private final WebClient targetServiceWebClient;

    public SomeDto callRemote(Long id) {
        return targetServiceWebClient.get()
                .uri("/api/path/{id}", id)
                .retrieve()
                .bodyToMono(SomeDto.class)
                .block();
    }
}
```

### Asynchronous (Kafka)

**Producer side:**
```java
@Service
@RequiredArgsConstructor
public class MyProducerService {
    private final KafkaTemplate<String, MyEntity> kafkaTemplate;

    public void publish(MyEntity entity) {
        kafkaTemplate.send("topic-name", entity);
    }
}
```

**Consumer side:**
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class MyConsumerListener {
    private final MyProcessingService processingService;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(MyEntity entity) {
        log.info("Received: {}", entity.getId());
        processingService.process(entity);
    }
}
```

## Checklist for New Endpoints

- [ ] Create/update DTO (request + response)
- [ ] Add service method with business logic
- [ ] Add controller endpoint returning `ResponseEntity<T>`
- [ ] Add validation annotations (`@NotBlank`, `@Email`, etc.) on request DTO
- [ ] Update `.claude/api-reference.md`
- [ ] Update `.claude/workflows.md` if new business flow
