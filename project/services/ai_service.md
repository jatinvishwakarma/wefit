# 🤖 AI Service — Kafka Consumer & Gemini AI Integration

> **Module:** `aiService/`
> **Spring Name:** `ai-service`
> **Port:** `8083`
> **Database:** MongoDB (`AiRecommendationsdb`)
> **Last Updated:** 2026-09-19

---

## Purpose

The AI Service is the **intelligence layer** of Wefit. It:

1. **Consumes** activity events from the Kafka `activity-events` topic asynchronously.
2. **Generates** personalised fitness recommendations using Google's Gemini 2.0 Flash API.
3. **Persists** structured recommendations (analysis, improvements, suggestions, safety) to MongoDB.
4. **Serves** recommendations to clients via REST endpoints.

This is a fully **event-driven** service — it has no user-facing write endpoints. All data flows in through Kafka.

---

## Directory Structure

```
aiService/
├── src/main/java/com/wefit/aiService/
│   ├── AiServiceApplication.java              ← Main Spring Boot class
│   ├── config/
│   │   ├── KafkaConfig.java                   ← Kafka consumer configuration
│   │   └── WebClientConfig.java               ← WebClient builder bean
│   ├── controller/
│   │   └── RecommendationController.java      ← REST endpoints for fetching recommendations
│   ├── entities/
│   │   ├── Activity.java                      ← Kafka message payload (mirror of Activity Service entity)
│   │   ├── ActivityType.java                  ← Enum (mirror)
│   │   └── Recommendation.java               ← MongoDB document for AI recommendations
│   ├── repositories/
│   │   └── RecommendationRepository.java      ← MongoDB repository
│   └── service/
│       ├── ActivityAiService.java             ← Core AI logic: prompt → Gemini → parse → save
│       ├── ActivityMessageListener.java       ← Kafka consumer listener
│       ├── GeminiService.java                 ← HTTP client for Gemini API
│       └── RecommendationService.java         ← Read service for recommendations
├── src/main/resources/
│   └── application.yml                        ← Bootstrap to Config Server
└── pom.xml
```

---

## How Data Flows Through This Service

```
Kafka Topic: "activity-events"
  ↓
ActivityMessageListener.listen(Activity)
  ↓
ActivityAiService.generateRecommendation(Activity)
  ↓
  ├── 1. Create structured prompt from activity data
  ├── 2. Call GeminiService.getRecommendations(prompt)
  │       ↓
  │       POST to Gemini API → raw JSON response
  ↓
  ├── 3. Parse Gemini response (strip markdown, extract fields)
  ├── 4. Build Recommendation entity
  └── 5. Save to MongoDB ("ai_recommendations" collection)

Later, client requests:
  GET /api/recommendations/user/{userId}     → RecommendationController
  GET /api/recommendations/activity/{activityId} → RecommendationController
```

---

## Database Schema

**Database:** MongoDB (`AiRecommendationsdb`)
**Collection:** `ai_recommendations`

### Recommendation Document Structure

```json
{
  "_id": "ObjectId('...')",
  "userId": 1,
  "activityId": "668abc123def...",            // Links to Activity Service's MongoDB ObjectId
  "recommendation": "Overall analysis text from AI",
  "improvements": [
    "Pace: Increase your average pace by...",
    "Hydration: Drink more water during..."
  ],
  "suggestions": [
    "Interval Training: Try alternating between...",
    "Hill Runs: Incorporate incline training..."
  ],
  "safetyPrecautions": [
    "Always warm up for 5-10 minutes before running",
    "Monitor heart rate and stay in zone 2-3"
  ],
  "createdAt": "2026-06-20T08:10:00"
}
```

---

## Entity: Recommendation.java

**Path:** `aiService/src/main/java/com/wefit/aiService/entities/Recommendation.java`

| Field               | Type            | MongoDB Mapping    | Notes                                      |
|---------------------|-----------------|--------------------|--------------------------------------------|
| `id`                | `String`        | `_id`              | MongoDB ObjectId                           |
| `userId`            | `Long`          | `userId`           | Links to User Service                      |
| `activityId`        | `String`        | `activityId`       | Links to Activity Service's MongoDB ID     |
| `recommendation`    | `String`        | `recommendation`   | Overall analysis text from Gemini          |
| `improvements`      | `List<String>`  | `improvements`     | Formatted as "Area: Recommendation"        |
| `suggestions`       | `List<String>`  | `suggestions`      | Formatted as "Workout: Description"        |
| `safetyPrecautions` | `List<String>`  | `safetyPrecautions`| Plain text safety tips                     |
| `createdAt`         | `LocalDateTime` | `createdAt`        | `@CreatedDate`                             |

---

## Entity: Activity.java (Kafka Payload Mirror)

**Path:** `aiService/src/main/java/com/wefit/aiService/entities/Activity.java`

This is a **mirror** of the Activity Service's `Activity` entity. It exists because:
- The AI Service and Activity Service are separate microservices with separate classpaths.
- There is no shared library. The Kafka consumer deserialises messages into this local class.
- The fields must match exactly (same names, types) for JSON deserialisation to work.

The Kafka consumer config (`spring.json.value.default.type: com.wefit.aiService.entities.Activity`) tells the `JsonDeserializer` to use this class.

---

## Service Layer (Class-by-Class)

### 1. ActivityMessageListener.java — Kafka Consumer

**Path:** `aiService/src/main/java/com/wefit/aiService/service/ActivityMessageListener.java`

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAiService activityAiService;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Activity activity) {
        log.info("Received Activity for processing: {}", activity.getId());
        activityAiService.generateRecommendation(activity);
    }
}
```

**Key details:**
- `@KafkaListener` subscribes to the `activity-events` topic with the consumer group `activity-processor-group`.
- The `Activity` parameter is automatically deserialised from JSON by Spring Kafka using the `JsonDeserializer` configured in `KafkaConfig`.
- This method is called **asynchronously** whenever a new message arrives on the topic.
- If multiple AI Service instances are running with the same `group-id`, Kafka will distribute messages across them (consumer group load balancing).

---

### 2. ActivityAiService.java — The AI Brain ⭐

**Path:** `aiService/src/main/java/com/wefit/aiService/service/ActivityAiService.java`

This is the **most complex class** in the AI Service.

#### `generateRecommendation(Activity)` — Main Entry Point

```
1. Create structured prompt from activity data
   (createPromptForActivity)
   ↓
2. Call GeminiService.getRecommendations(prompt)
   ↓ (success)              ↓ (failure)
3a. processAiResponse()     3b. Save fallback recommendation
                                "We couldn't connect to the AI service,
                                 but keep up the good work!"
```

#### `createPromptForActivity(Activity)` — Prompt Engineering

The prompt uses a Java **text block** (multi-line string) and requests Gemini to respond in a very specific JSON format:

```json
{
  "analysis": {
    "overall": "Overall analysis here",
    "pace": "Pace analysis here",
    "heartRate": "Heart rate analysis here",
    "caloriesBurned": "Calories analysis here"
  },
  "improvements": [
    { "area": "Area name", "recommendation": "Detailed recommendation" }
  ],
  "suggestions": [
    { "workout": "Workout name", "description": "Detailed workout description" }
  ],
  "safety": [
    "Safety point 1", "Safety point 2"
  ]
}
```

The activity data injected into the prompt:
- Activity Type (e.g., `RUNNING`)
- Duration in minutes
- Calories Burned
- Additional Metrics (the `Map<String, Object>`, printed as `.toString()`)

#### `processAiResponse(String, Activity)` — Response Parsing

This method handles the notoriously inconsistent output from LLMs:

```
1. Parse raw Gemini API response as JSON
   ↓
2. Navigate: candidates[0].content.parts[0].text
   ↓
3. Strip markdown code fences: ```json ... ```
   ↓
4. Remove escaped newlines: \\n
   ↓
5. Parse the cleaned string as JSON
   ↓
6. Extract fields:
   - analysis.overall → recommendation (String)
   - improvements[] → List<String> formatted as "area: recommendation"
   - suggestions[] → List<String> formatted as "workout: description"
   - safety[] → List<String> (plain text)
   ↓
7. Build Recommendation entity and save to MongoDB
```

**Why the double-parse?** Gemini's API returns a wrapper JSON structure (`candidates[0].content.parts[0].text`). The `text` field contains the actual AI response as a **string** (which itself is JSON). So we have to parse JSON to get a string, then parse that string as JSON again.

**Why strip markdown fences?** Gemini often wraps JSON responses in ` ```json ... ``` ` markdown code blocks, even when asked not to. The `replaceAll` calls handle this.

---

### 3. GeminiService.java — HTTP Client for Gemini API

**Path:** `aiService/src/main/java/com/wefit/aiService/service/GeminiService.java`

```java
public String getRecommendations(String prompt) {
    Map<String, Object> requestBody = Map.of(
            "contents", new Object[]{
                    Map.of("parts", new Object[]{
                            Map.of("text", prompt)
                    })
            });

    return restClient
            .post()
            .uri(geminiApiUrl + "?key=" + geminiApiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(String.class);
}
```

**Key details:**
- Uses `RestClient` (synchronous, introduced in Spring 6.1) instead of `WebClient` (reactive) — simpler since this is called synchronously from the Kafka listener.
- The API URL is `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent`.
- The API key is passed as a query parameter (`?key=...`).
- The request body follows the Gemini API's expected format: `{ "contents": [{ "parts": [{ "text": "..." }] }] }`.
- Returns the raw JSON string response for parsing by `ActivityAiService`.

---

### 4. RecommendationService.java — Read Service

**Path:** `aiService/src/main/java/com/wefit/aiService/service/RecommendationService.java`

Simple read-only service:

| Method                                | What it does                                              |
|---------------------------------------|-----------------------------------------------------------|
| `getUserRecommendations(Long userId)` | Returns the **top 5** most recent recommendations for a user |
| `getActivityRecommendation(String activityId)` | Returns the recommendation for a specific activity |

---

### 4. Exception Handling

**Path:** `aiService/src/main/java/com/wefit/aiService/exception/`

- **`AiProcessingException.java`**: Thrown during errors communicating with Gemini.
- **`GlobalExceptionHandler.java`**: Uses `@RestControllerAdvice` to standardize all errors into RFC 7807 `ProblemDetail` JSON responses, injecting a `correlationId` and masking internal stack traces.

## API Endpoints

### RecommendationController (`api/recommendations`)

| Method | Path                                       | Description                                  | Response                       |
|--------|--------------------------------------------|----------------------------------------------|--------------------------------|
| `GET`  | `api/recommendations/user/{userId}`        | Get top 5 recommendations for a user         | `List<Recommendation>` (200)   |
| `GET`  | `api/recommendations/activity/{activityId}` | Get recommendation for a specific activity  | `Recommendation` (200)         |

**Note:** The `@RequestMapping` path does NOT start with `/` — it's `api/recommendations` not `/api/recommendations`. This works because Spring normalises it, but it's inconsistent with other services.

---

## Repository: RecommendationRepository.java

```java
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    List<Recommendation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    Recommendation findByActivityId(String activityId);
}
```

| Method                                         | What it does                                              |
|------------------------------------------------|-----------------------------------------------------------|
| `findTop5ByUserIdOrderByCreatedAtDesc(userId)` | Returns the 5 most recent recommendations for a user (sorted by `createdAt` descending) |
| `findByActivityId(activityId)`                 | Returns the single recommendation linked to an activity   |

Spring Data MongoDB auto-generates the query from the method name. `Top5` limits results to 5, `OrderByCreatedAtDesc` sorts by the `createdAt` field in descending order.

---

## Kafka Consumer Configuration

### KafkaConfig.java

**Path:** `aiService/src/main/java/com/wefit/aiService/config/KafkaConfig.java`

Creates a `ConsumerFactory` and `ConcurrentKafkaListenerContainerFactory`:

```java
ConsumerFactory<String, Activity> consumerFactory() {
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

    JsonDeserializer<Activity> jsonDeserializer = new JsonDeserializer<>(Activity.class);
    jsonDeserializer.addTrustedPackages("*");      // Trust all packages
    jsonDeserializer.setUseTypeHeaders(false);     // Don't use Java type headers from producer
}
```

**Critical configuration explained:**

| Setting                 | Value    | Why                                                                   |
|-------------------------|---------|-----------------------------------------------------------------------|
| `addTrustedPackages("*")` | `*`   | Accept messages from any Java package. Required because the Activity Service produces `com.wefit.activityService.entities.Activity` but this service deserialises as `com.wefit.aiService.entities.Activity` (different package!) |
| `setUseTypeHeaders(false)` | `false` | Ignore the `__TypeId__` header from the producer. If this were `true`, the consumer would try to deserialise to the producer's class (`com.wefit.activityService.entities.Activity`), which doesn't exist on the consumer's classpath. |

The Config Server also sets:
```yaml
spring.json.value.default.type: com.wefit.aiService.entities.Activity
spring.json.trusted.packages: "*"
```

This tells the consumer to always deserialise to the local `Activity` class.

---

## Configuration

From Config Server (`config/ai-service.yml`):

| Property                        | Value                                                    |
|---------------------------------|----------------------------------------------------------|
| `spring.data.mongodb.uri`       | `${MONGODB_URI_AI_SERVICE:mongodb://localhost:27017/AiRecommendationsdb}` |
| `spring.data.mongodb.database`  | `${MONGODB_DATABASE_AI_SERVICE:AiRecommendationsdb}`     |
| `spring.kafka.bootstrap-servers`| `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`               |
| `spring.kafka.consumer.group-id`| `${KAFKA_CONSUMER_GROUP_ID:activity-processor-group}`    |
| `kafka.topic.name`              | `${KAFKA_TOPIC_NAME:activity-events}`                    |
| `gemini.api.url`                | Gemini v1beta generateContent URL                        |
| `gemini.api.key`                | `${GEMINI_API_KEY:}`                                     |
| `server.port`                   | `8083`                                                   |
| SSL                             | Enabled (PKCS12 keystore)                                |

---

## Design Decisions

1. **Event-driven (Kafka) over synchronous REST:** If the Activity Service called the AI Service synchronously, the user would wait 5-10 seconds for Gemini to respond. With Kafka, the activity is saved immediately and the AI processes it in the background. The user can fetch recommendations later.
2. **Fallback recommendation:** If Gemini is down or returns an error, a generic fallback recommendation is saved. This ensures no activity goes unprocessed and the user always sees something.
3. **Mirror entities (not shared library):** The `Activity` entity is duplicated in this service. A shared Maven module would be cleaner but adds deployment coupling. For now, duplication is the pragmatic choice.
4. **Top 5 limit:** `findTop5ByUserIdOrderByCreatedAtDesc` prevents the API from returning thousands of recommendations. This can be made pageable in the future.
5. **`RestClient` for Gemini (not `WebClient`):** Since the Kafka listener runs on a worker thread (not a reactive pipeline), a synchronous `RestClient` is simpler and avoids unnecessary reactive complexity.
6. **No retry/dead-letter queue:** If Kafka consumption fails, the message is lost. Future improvements should add a dead-letter topic and retry logic.
