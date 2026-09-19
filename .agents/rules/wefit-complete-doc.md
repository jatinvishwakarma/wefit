# WeFit — Complete Technical Specification & Source Code Documentation

This document serves as the absolute master reference for the WeFit platform. It provides a complete, exhaustive, method-by-method, file-by-file breakdown of the entire microservices architecture. It includes deep technical explanations of how every component is wired, how data is passed, and the responsibilities of every single file in the project.

---

## 1. The Big Picture: Architectural Flow & Data Lifecycle

WeFit is a polyglot microservices system built on Spring Boot 3.x, utilizing Spring WebFlux for reactive API Gateway routing and Spring WebMVC for internal blocking data operations in downstream services.

### The Complete Data Lifecycle: Adding an Activity

1. **Client Authorization**: A user logs in via **Keycloak** (OAuth2/OIDC provider). Keycloak returns an Access Token (JWT).
2. **API Gateway Ingress (`:8080`)**: 
   - The client sends an HTTP `POST /api/activities/add` request with the `Authorization: Bearer <JWT>`.
   - The Gateway's `SecurityConfiguration` validates the JWT signature natively via Spring Security's OAuth2 Resource Server.
   - The `KeyCloakUserSyncFilter` intercepts the request. Using the Nimbus JOSE library, it manually parses the JWT payload to extract `sub` (Keycloak ID), `email`, `given_name`, etc.
   - The Gateway makes a non-blocking `WebClient` call to `UserService` to check if this Keycloak ID exists in the local PostgreSQL DB. If not found (404), it triggers an auto-registration `POST` request to create the user locally.
   - The Gateway then mutates the original incoming request, injecting `X-User-Id` (the local PostgreSQL primary key) and `X-User-Keycloak-Id` into the HTTP headers, and proxies the request to `ActivityService`.
3. **Activity Processing (`:8082`)**:
   - `ActivityController.addActivity()` receives the incoming payload (`ActivityRequestDto`).
   - `ActivityService.addActivity()` halts execution to perform a synchronous `WebClient` call back to `UserService` (`/api/user/auth/{userId}/validate`) to double-check that the user ID provided hasn't been tampered with or deleted.
   - If valid, the DTO is converted to an `Activity` Document entity and saved to **MongoDB** (`WefitActivitydb`).
4. **Event Sourcing (Kafka)**:
   - After the MongoDB write successfully commits, `ActivityService` uses `KafkaTemplate` to serialize the saved `Activity` object into JSON and publishes it to the `activity-events` Kafka topic.
   - The HTTP thread returns the saved `ActivityResponseDto` to the client. The client is **not** blocked waiting for the AI response.
5. **AI Generation (`:8083`)**:
   - `AiService`'s `ActivityMessageListener` constantly listens to the `activity-events` topic. It consumes the JSON payload, deserializing it back into an `Activity` object.
   - `ActivityAiService` constructs a massive text prompt using string interpolation (`createPromptForActivity()`), injecting the activity's duration, calories, extra metrics, and type. It strictly instructs the LLM to reply in a specific JSON structure.
   - `GeminiService` uses Spring's synchronous `RestClient` to issue a POST request to Google's Generative AI API (`gemini-pro`).
   - The AI returns a structured JSON string. `processAiResponse()` uses Jackson's `ObjectMapper` to traverse the JSON tree, extract arrays (improvements, suggestions, safety), map them to a `Recommendation` MongoDB Document, and save it to the `AiRecommendationsdb` database.

---

## 2. API Gateway Service (`com.wefit.apiGateway`)

The Gateway is the single point of ingress. It uses Spring Cloud Gateway (Reactive/WebFlux).

### `ApiGatewayApplication.java`
- **Purpose**: Bootstrap class.
- **Annotations**: `@SpringBootApplication`. Standard entry point.

### `SecurityConfiguration.java`
- **Purpose**: Defines global edge security.
- **Annotations**: `@Configuration`, `@EnableWebFluxSecurity`.
- **Method: `springSecurityFilterChain(ServerHttpSecurity http)`**:
  - **Technical Details**: Disables CSRF (`csrfSpec::disable`) because this is a stateless REST API (no cookies/sessions). Permits all traffic to `/actuator/**` for health checks. Requires authentication for all other exchanges. Enables `oauth2ResourceServer().jwt()` telling Spring to validate incoming tokens against the Keycloak JWKS URI defined in `application.yml`.

### `filter/KeyCloakUserSyncFilter.java`
- **Purpose**: The most complex class in the Gateway. It guarantees that any user authenticated by Keycloak also exists in the local `UserService` database before any downstream service sees the request.
- **Annotations**: `@Component`, `@Order(1)` (executes early in the filter chain). Implements `WebFilter`.
- **Method: `filter(ServerWebExchange exchange, WebFilterChain chain)`**:
  - **Technical Details**: Extracts the `Authorization` header. If missing, skips. If present, calls `extractUserDetails()`. Then calls `lookupUserByKeycloakId()`. If the WebClient throws a `WebClientResponseException.NotFound` (404), it catches it using reactor's `.onErrorResume()` and calls `registerNewUser()`. Finally, uses `exchange.getRequest().mutate()` to append `X-User-Id` headers.
- **Method: `lookupUserByKeycloakId(String keycloakId)`**:
  - **Technical Details**: Non-blocking WebClient `GET` request to `http://localhost:8081/api/users/keycloak/{keycloakId}`.
- **Method: `registerNewUser(UserRequestDto userDetails)`**:
  - **Technical Details**: Sets a dummy password (`KEYCLOAK_MANAGED`) since authentication is externalized. Makes a non-blocking `POST` to `/api/users/register`.
- **Method: `extractUserDetails(String token)`**:
  - **Technical Details**: Bypasses Spring Security context. Directly uses `SignedJWT.parse(jwt)` from the Nimbus JOSE library to extract claims (`sub`, `email`, `given_name`, `family_name`, `preferred_username`).
- **Inner Class: `UserSyncResponse`**:
  - **Purpose**: Minimal DTO used only to deserialize the internal `id` from the `UserService` JSON response.

### `user/UserRequestDto.java`, `user/UserRole.java`, `user/WebClientConfig.java`
- **Purpose**: Local copies of DTOs and Enums. `WebClientConfig` creates a `@LoadBalanced` (or direct) `WebClient.Builder` bean so the filter can perform HTTP requests to `UserService`.

---

## 3. User Service (`com.wefit.userService`)

Manages relational user data, utilizing **PostgreSQL** and Spring Data JPA.

### `UserServiceApplication.java`
- **Purpose**: Bootstrap class.

### `controller/AuthController.java`
- **Purpose**: Exposes endpoints specifically for authentication and validation flows.
- **Method: `registerUser(@Valid @RequestBody UserRequestDto)`**:
  - **Technical Details**: `POST /api/user/auth/register`. Calls `userService.registerUser()`. Returns HTTP 200 with `UserResponseDto`.
- **Method: `validateUser(@PathVariable Long userId)`**:
  - **Technical Details**: `GET /api/user/auth/{userId}/validate`. Fast path used by `ActivityService`. Calls `userService.existsById()`. Returns a simple boolean.

### `controller/UserController.java`
- **Purpose**: Exposes endpoints for profile retrieval and management.
- **Method: `registerUser()`**: Duplicate of AuthController's register for Gateway use.
- **Method: `getUserByKeycloakId(@PathVariable String keycloakId)`**:
  - **Technical Details**: `GET /api/users/keycloak/{keycloakId}`. Calls `userService.getUserByKeycloakId()`.
- **Method: `getUserProfile(@PathVariable String identifier)`**:
  - **Technical Details**: `GET /api/users/profile/{identifier}`. Allows searching by either username or email.
- **Method: `getUserById(@PathVariable Long id)`**:
  - **Technical Details**: `GET /api/users/{id}`.

### `service/UserService.java`
- **Purpose**: The core business logic layer.
- **Method: `registerUser(UserRequestDto)`**:
  - **Technical Details**: Uses `Optional<User>` to check if the email exists. 
    - **Logic Branch 1**: If email exists but `keycloakId` is null, it updates the existing row with the Keycloak ID (Account Linking).
    - **Logic Branch 2**: If email exists and `keycloakId` differs, throws a RuntimeException (security precaution).
    - **Logic Branch 3**: If email doesn't exist, uses Lombok's builder to construct a new `User` entity, applies the default role (`UserRole.USER`), sets timestamps, and saves via `userRepository.save()`.
- **Method: `getUserProfile(String identifier)`**:
  - **Technical Details**: Calls `findByUserNameOrEmail`. If empty, throws `ResourceNotFoundException`.
- **Method: `getUserById(Long)` & `getUserByKeycloakId(String)`**:
  - **Technical Details**: Standard wrapper lookups throwing 404 exceptions on miss.
- **Method: `convertToResponseDto(User user)`**:
  - **Technical Details**: Private mapper method. Transforms the JPA entity into the safe `UserResponseDto`, stripping out the `password` field.

### `repository/UserRepository.java`
- **Purpose**: Spring Data JPA interface extending `JpaRepository<User, Long>`.
- **Methods**: `findByUserNameOrEmail`, `findByEmail`, `findByKeycloakId`, `existsByEmail`, `existsByKeycloakId`.
- **Technical Details**: Spring Data automatically implements these methods at runtime based on the method names, generating the corresponding PostgreSQL SQL queries.

### `entities/User.java`
- **Purpose**: The physical mapping to the `users` table in PostgreSQL.
- **Annotations**: `@Entity`, `@Table(name = "users")`.
- **Fields**: 
  - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`: Auto-incrementing primary key.
  - `@Column(unique = true)`: Enforced unique constraints on `userName`, `email`, and `keycloakId`.
  - `@CreationTimestamp`, `@UpdateTimestamp`: Hibernate automatically manages these fields on `INSERT` and `UPDATE`.
- **Methods**: `fromEntity(UserRequestDto)`, `toDto(User)`.

### `entities/UserRole.java`
- **Purpose**: Enum mapping to string values in DB.

### `dto/UserRequestDto.java` & `dto/UserResponseDto.java`
- **Purpose**: Data carriers. `UserRequestDto` utilizes `jakarta.validation` annotations like `@NotBlank` and `@Email` to automatically reject bad payloads at the Controller level.

### `exception/ResourceNotFoundException.java`
- **Purpose**: Mapped to `@ResponseStatus(HttpStatus.NOT_FOUND)`. Crucial for inter-service communication because when thrown, Spring WebMVC translates it to a 404 HTTP response, which the Gateway catches.

---

## 4. Activity Service (`com.wefit.activityService`)

Manages physical activities. Utilizes **MongoDB** for unstructured metrics and **Kafka** for messaging.

### `ActivityServiceApplication.java`
- **Purpose**: Bootstrap class.

### `config/KafkaConfig.java`
- **Purpose**: Configures the Kafka Producer.
- **Method: `producerFactory()` & `kafkaTemplate()`**:
  - **Technical Details**: Creates a `DefaultKafkaProducerFactory`. Sets the `BOOTSTRAP_SERVERS_CONFIG` to `localhost:9092`. Sets the `KEY_SERIALIZER_CLASS_CONFIG` to `StringSerializer` and `VALUE_SERIALIZER_CLASS_CONFIG` to `JsonSerializer`. This ensures Java objects are sent over the network as valid JSON strings.

### `config/MongoConfigurations.java`
- **Purpose**: Contains `@EnableMongoAuditing`.
- **Technical Details**: Required so Spring Data MongoDB knows to populate the `@CreatedDate` and `@LastModifiedDate` annotations on the `Activity` entity automatically upon `save()`.

### `config/WebClientConfig.java`
- **Purpose**: Provides a `WebClient` pointing to the UserService base URL.

### `controller/ActivityController.java`
- **Purpose**: Exposes activity creation.
- **Method: `addActivity(@RequestBody ActivityRequestDto)`**: POST mapping for `/api/activities/add`.

### `service/ActivityService.java`
- **Purpose**: Core business logic.
- **Method: `addActivity(ActivityRequestDto activityRequestDto)`**:
  - **Technical Details**: 
    1. Synchronous blocking call: `userValidationService.validateUser(userId)`. Throws RuntimeException if false.
    2. Uses static `fromEntity()` to map DTO to `Activity`.
    3. `activityRepository.save()`: Writes to MongoDB.
    4. Asynchronous call: `kafkaTemplate.send(topicName, savedActivity)`. Non-blocking fire-and-forget message push to Kafka.

### `service/UserValidationService.java`
- **Purpose**: Handles HTTP communication with `UserService`.
- **Method: `validateUser(Long userId)`**:
  - **Technical Details**: Uses `WebClient.get().uri(...).retrieve().bodyToMono().block()`. The `.block()` is used to convert the reactive Mono stream into a synchronous result because the ActivityService is built on standard WebMVC (blocking threads). Returns false if an exception (like 404) occurs.

### `repositories/ActivityRepository.java`
- **Purpose**: Extends `MongoRepository<Activity, String>`.

### `entities/Activity.java`
- **Purpose**: MongoDB Document mapping.
- **Annotations**: `@Document("Activities")`.
- **Fields**: 
  - `@Id private String id`: Mongo uses 24-character Hex strings.
  - `@Field("metrics") private Map<String, Object> additionalMetrics`: A highly flexible schema-less JSON object allowing clients to store arbitrary keys (e.g., `{"heartRateMax": 180, "avgSpeed": 12.5}`).
  - `@CreatedDate`, `@LastModifiedDate`: Auto-managed timestamps.

### `entities/ActivityType.java`, `dto/ActivityRequestDto.java`, `dto/ActivityResponseDto.java`
- **Purpose**: Standard enumerations (RUNNING, YOGA, etc.) and payload mappings.

---

## 5. AI Service (`com.wefit.aiService`)

The brain of the operation. Completely decoupled from client HTTP requests. Driven entirely by Kafka events.

### `AiServiceApplication.java`
- **Purpose**: Bootstrap class.

### `config/KafkaConfig.java`
- **Purpose**: Configures the Kafka Consumer.
- **Method: `consumerFactory()` & `kafkaListenerContainerFactory()`**:
  - **Technical Details**: Sets up the consumer group `activity-processor-group`. Crucially, it configures `JsonDeserializer.TRUSTED_PACKAGES` to `"*"` and sets the default mapping type to `com.wefit.aiService.entities.Activity.class`. This is required because the class package name sent by ActivityService (`com.wefit.activityService.entities.Activity`) differs from the local package, and Jackson needs permission to deserialize it.

### `config/WebClientConfig.java`
- **Purpose**: Provides `RestClient.Builder`. (Uses `RestClient` rather than `WebClient` due to JDK21/Windows SSL bug workarounds).

### `controller/RecommendationController.java`
- **Purpose**: Read-only API for clients to fetch AI results.
- **Methods**: `getUserRecommendations(Long userId)` and `getActivityRecommendation(String activityId)`.

### `service/ActivityMessageListener.java`
- **Purpose**: The entry point for Kafka consumption.
- **Method: `listen(Activity activity)`**:
  - **Technical Details**: Annotated with `@KafkaListener`. Spring automatically invokes this method on a dedicated background thread pool whenever a message arrives on the configured topic. Passes the payload to `ActivityAiService`.

### `service/GeminiService.java`
- **Purpose**: Wrapper for the Google Gemini API.
- **Method: `getRecommendations(String prompt)`**:
  - **Technical Details**: Constructs a complex nested `Map<String, Object>` that matches the strict payload structure required by Gemini (`{"contents": [{"parts": [{"text": prompt}]}]}`). Uses `RestClient` to issue a synchronous `POST` request. Returns the raw JSON body as a `String`.

### `service/ActivityAiService.java`
- **Purpose**: Orchestration and Prompt Engineering.
- **Method: `generateRecommendation(Activity activity)`**:
  - **Technical Details**: Try-catch wrapper. Calls `createPromptForActivity`, passes it to `GeminiService`. Passes result to `processAiResponse`. If Google APIs are down, it builds a `Recommendation` object with a hardcoded fallback string and saves it.
- **Method: `createPromptForActivity(Activity activity)`**:
  - **Technical Details**: Uses Java 15+ Text Blocks (`"""`) to craft a prompt. Forces the LLM to output an "EXACT JSON format". Injects variables using `String.format()`.
- **Method: `processAiResponse(String aiResponse, Activity activity)`**:
  - **Technical Details**: 
    1. Uses `ObjectMapper` to parse the raw string into a `JsonNode`.
    2. Navigates Google's wrapper tree: `candidates[0].content.parts[0].text`.
    3. The LLM often wraps JSON in markdown blockticks (```` ```json ````). It uses regex `.replaceAll()` to strip these out.
    4. Parses the inner JSON string into a new `JsonNode`.
    5. Iterates over JSON Arrays (`improvements`, `suggestions`, `safety`) and converts them into Java `List<String>`.
    6. Builds the `Recommendation` entity using Lombok `@Builder`.
    7. Calls `recommendationRepository.save()`.

### `service/RecommendationService.java`
- **Purpose**: Wrapper around the repository.

### `repositories/RecommendationRepository.java`
- **Purpose**: Extends `MongoRepository<Recommendation, String>`.
- **Methods**: `findTop5ByUserIdOrderByCreatedAtDesc(Long userId)`, `findByActivityId(String activityId)`. Spring Data translates these into Mongo sorting/limiting queries.

### `entities/Recommendation.java`
- **Purpose**: MongoDB Document mapping.
- **Annotations**: `@Document(collection = "Recommendations")`.
- **Fields**: Includes `userId`, `activityId`, `recommendation` (overall text), `List<String> improvements`, `List<String> suggestions`, `List<String> safetyPrecautions`.

### `entities/Activity.java` & `entities/ActivityType.java`
- **Purpose**: Mirrored DTOs. AiService maintains its own local copy of the Activity entity so the Kafka consumer knows how to map the incoming JSON.

---

## 6. Infrastructure Services

### Config Server (`com.wefit.configServer`)
- **File**: `ConfigServerApplication.java`
- **Technical Details**: Contains `@EnableConfigServer`. Bound to port `8888`. It exposes externalized properties (e.g., from a git repository, classpath, or native files) via HTTP so that microservices can pull their configurations dynamically upon startup.

### Eureka Server (`com.wefit.eureka`)
- **File**: `EurekaApplication.java`
- **Technical Details**: Contains `@EnableEurekaServer`. Bound to port `8761`. Microservices include the `spring-cloud-starter-netflix-eureka-client` dependency. On boot, they send a heartbeat registration to this server, advertising their IP and Port. Other services can query Eureka to resolve abstract names (e.g., `USER-SERVICE`) to physical URLs, facilitating client-side load balancing.
