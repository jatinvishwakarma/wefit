# 🏃 Activity Service — Workout Logging & Kafka Producer

> **Module:** `activityService/`
> **Spring Name:** `activity-service`
> **Port:** `8082`
> **Database:** MongoDB (`WefitActivitydb`)
> **Last Updated:** 2026-09-19

---

## Purpose

The Activity Service is the **core fitness tracking engine** of Wefit. It:

1. Receives workout data from users (running, cycling, yoga, etc.).
2. Validates that the user exists by calling the User Service synchronously.
3. Persists the activity to MongoDB.
4. Publishes the saved activity to Kafka for asynchronous processing by the AI Service.

This service is the **bridge** between the user-facing action (logging a workout) and the AI-powered analysis (generating recommendations).

---

## Directory Structure

```
activityService/
├── src/main/java/com/wefit/activityService/
│   ├── ActivityServiceApplication.java    ← Main Spring Boot class
│   ├── config/
│   │   ├── KafkaConfig.java               ← Kafka producer configuration
│   │   ├── MongoConfigurations.java       ← Enables MongoDB auditing
│   │   └── WebClientConfig.java           ← WebClient for User Service calls
│   ├── controller/
│   │   └── ActivityController.java        ← REST endpoint for adding activities
│   ├── dto/
│   │   ├── ActivityRequestDto.java        ← Input DTO
│   │   └── ActivityResponseDto.java       ← Output DTO
│   ├── entities/
│   │   ├── Activity.java                  ← MongoDB document entity
│   │   └── ActivityType.java              ← Enum of supported activity types
│   ├── repositories/
│   │   └── ActivityRepository.java        ← MongoDB repository
│   └── service/
│       ├── ActivityService.java           ← Core business logic
│       └── UserValidationService.java     ← Synchronous User Service call
├── src/main/resources/
│   └── application.yml                    ← Bootstrap to Config Server
└── pom.xml
```

---

## Database Schema

**Database:** MongoDB (`WefitActivitydb`)
**Collection:** `Activities`
**ORM:** Spring Data MongoDB

### Activity Document Structure

```json
{
  "_id": "ObjectId('...')",                // MongoDB auto-generated string ID
  "activityType": "RUNNING",              // Enum (see ActivityType below)
  "durationInMinutes": 30,
  "userId": 1,                            // Links to User Service (Long)
  "caloriesBurned": 280,
  "startTime": "2026-06-20T08:00:00",     // ISO 8601
  "metrics": {                            // Flexible, unstructured data
    "distanceKm": 5,
    "avgHeartRate": 145
  },
  "createdAt": "2026-06-20T08:05:00",     // Auto-set by @CreatedDate
  "updatedAt": "2026-06-20T08:05:00"      // Auto-set by @LastModifiedDate
}
```

### Why MongoDB?

Activities have **variable schemas** — a running session has `distanceKm`, a weightlifting session has `reps` and `sets`, yoga has `poses`. The `additionalMetrics` field is a `Map<String, Object>` that can hold anything. MongoDB's document model handles this naturally without schema migrations.

---

## Entity: Activity.java

**Path:** `activityService/src/main/java/com/wefit/activityService/entities/Activity.java`

| Field               | Type                    | MongoDB Mapping           | Notes                                  |
|---------------------|-------------------------|---------------------------|----------------------------------------|
| `id`                | `String`                | `_id` (`@Id`)             | MongoDB ObjectId as string             |
| `activityType`      | `ActivityType`          | Stored as string          | Enum value                             |
| `durationInMinutes` | `Integer`               | `durationInMinutes`       |                                        |
| `userId`            | `Long`                  | `userId`                  | Foreign key to User Service            |
| `caloriesBurned`    | `int`                   | `caloriesBurned`          |                                        |
| `startTime`         | `LocalDateTime`         | `startTime`               | When the user started the workout      |
| `additionalMetrics` | `Map<String, Object>`   | `metrics` (`@Field`)      | Flexible key-value pairs               |
| `createdAt`         | `LocalDateTime`         | `createdAt` (`@CreatedDate`) | Auto-set on insert                 |
| `updatedAt`         | `LocalDateTime`         | `updatedAt` (`@LastModifiedDate`) | Auto-set on update           |

**Static method:** `Activity.fromEntity(ActivityRequestDto)` — Builder-based factory for converting DTO to entity.

**Important:** The `id` field is `String`, NOT `Long`. This was a deliberate migration from an earlier version. MongoDB uses ObjectId internally, and mapping it to `String` avoids auto-generation conflicts that occur with `Long`.

---

## ActivityType Enum

```java
public enum ActivityType {
    RUNNING, WALKING, CYCLING, SWIMMING, YOGA,
    MEDITATION, HIIT, STRENGTH_TRAINING, CARDIO,
    FLEXIBILITY, OTHER
}
```

The `OTHER` value acts as a catch-all for activity types not yet in the enum.

---

## DTOs

### ActivityRequestDto (Input)

| Field               | Type                   | Notes                                    |
|---------------------|------------------------|------------------------------------------|
| `activityType`      | `ActivityType`         | Required                                 |
| `durationInMinutes` | `Integer`              | Duration of the workout                  |
| `userId`            | `Long`                 | Which user logged this activity          |
| `caloriesBurned`    | `int`                  | Calories burned during workout           |
| `startTime`         | `LocalDateTime`        | When the workout started                 |
| `additionalMetrics` | `Map<String, Object>`  | Flexible metrics (distance, heart rate, etc.) |

**Note:** No `@Valid` annotations on the request DTO — validation is done at the service layer (user existence check).

### ActivityResponseDto (Output)

Same fields as the entity plus `id`, `createdAt`, and `updatedAt`. Has a static `toDto(Activity)` factory method.

---
### 4. Exception Handling

**Path:** `activityService/src/main/java/com/wefit/activityService/exception/`

- **`ActivityNotFoundException.java`**: Thrown when an activity lookup fails.
- **`InvalidActivityException.java`**: Thrown during creation if the associated user is invalid.
- **`GlobalExceptionHandler.java`**: Uses `@RestControllerAdvice` to standardize all errors into RFC 7807 `ProblemDetail` JSON responses, injecting a `correlationId` and masking internal stack traces.
## API Endpoints

### ActivityController (`/api/activities`)

| Method | Path                     | Description                | Request Body          | Response                 |
|--------|--------------------------|----------------------------|-----------------------|--------------------------|
| `POST` | `/api/activities/add`   | Log a new workout activity | `ActivityRequestDto`  | `ActivityResponseDto` (200) |

This is currently the **only** endpoint. Future endpoints will include:
- `GET /api/activities/user/{userId}` — Fetch all activities for a user.
- `GET /api/activities/{id}` — Fetch a specific activity.
- `DELETE /api/activities/{id}` — Delete an activity.

---

## Service Layer: ActivityService.java

**Path:** `activityService/src/main/java/com/wefit/activityService/service/ActivityService.java`

### `addActivity(ActivityRequestDto)` — The Core Flow

```
1. Call UserValidationService.validateUser(userId)
   → GET /api/user/auth/{userId}/validate → User Service
   → Returns true/false
   ↓
2. If false → throw RuntimeException("Invalid user")
   ↓
3. Convert ActivityRequestDto → Activity entity
   (via Activity.fromEntity())
   ↓
4. Save to MongoDB
   (activityRepository.save(activity))
   ↓
5. Publish saved Activity to Kafka topic
   (kafkaTemplate.send(topicName, savedActivity))
   ↓
6. Convert saved Activity → ActivityResponseDto
   ↓
7. Return to client
```

**Critical design detail:** The Kafka message is sent **after** the MongoDB save. This means the AI Service will receive the activity with its MongoDB-generated `id` field populated, which it uses as the `activityId` in the Recommendation entity.

---

## UserValidationService.java

**Path:** `activityService/src/main/java/com/wefit/activityService/service/UserValidationService.java`

A dedicated service class for synchronous REST calls to the User Service:

```java
public boolean validateUser(Long userId) {
    try {
        Boolean isValid = userServiceWebClient.get()
                .uri("/api/user/auth/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();                          // ← Blocking call!
        return isValid != null && isValid;
    } catch (Exception e) {
        return false;                              // ← Fail-closed: treat errors as invalid
    }
}
```

**Key points:**
- Uses `WebClient` (reactive) but calls `.block()` to make it synchronous. This is acceptable in the service layer but should be avoided in WebFlux/reactive pipelines.
- **Fail-closed:** If the User Service is down or returns an error, the user is treated as invalid (activity is rejected). This prevents orphaned activities for non-existent users.

---

## Configuration Classes

### KafkaConfig.java (Producer)

Creates a `KafkaTemplate<Object, Object>` bean with:

| Property                           | Value                  | Why                                    |
|------------------------------------|------------------------|----------------------------------------|
| `bootstrap.servers`                | From env               | Kafka broker address                   |
| `key.serializer`                   | `StringSerializer`     | Keys are simple strings                |
| `value.serializer`                 | `JsonSerializer`       | Activity objects serialised as JSON    |

The Config Server also sets `spring.json.add.type.headers: false` — this prevents the producer from adding Java class type headers to the Kafka message, which would break the consumer if it has different class packages.

### MongoConfigurations.java

```java
@Configuration
@EnableMongoAuditing
public class MongoConfigurations { }
```

This single annotation (`@EnableMongoAuditing`) is what makes `@CreatedDate` and `@LastModifiedDate` work on the `Activity` entity. Without it, those fields would always be `null`.

### WebClientConfig.java

Creates a `WebClient` bean pointed at User Service:

```java
@Bean
public WebClient userServiceWebClient() {
    return webClientBuilder().baseUrl(userServiceBaseUrl).build();
}
```

`userServiceBaseUrl` defaults to `http://localhost:8081`, overridden by Config Server to `https://localhost:8081`.

---

## Repository: ActivityRepository.java

```java
@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> { }
```

Currently has **no custom query methods** — only uses the inherited `save()` method. Future additions:
- `findByUserId(Long userId)` — for listing a user's activities.
- `findByUserIdOrderByCreatedAtDesc(Long userId)` — for sorted listing.

---

## Configuration

From Config Server (`config/activity-service.yml`):

| Property                        | Value                                                    |
|---------------------------------|----------------------------------------------------------|
| `spring.data.mongodb.uri`       | `${MONGODB_URI_ACTIVITY_SERVICE:mongodb://localhost:27017/WefitActivitydb}` |
| `spring.data.mongodb.database`  | `${MONGODB_DATABASE_ACTIVITY_SERVICE:WefitActivitydb}`   |
| `spring.kafka.bootstrap-servers`| `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`               |
| `kafka.topic.name`              | `${KAFKA_TOPIC_NAME:activity-events}`                    |
| `user-service.base-url`         | `${USER_SERVICE_BASE_URL:https://localhost:8081}`         |
| `server.port`                   | `8082`                                                   |
| SSL                             | Enabled (PKCS12 keystore)                                |

---

## Design Decisions

1. **MongoDB for activities:** Activities have variable metrics (`Map<String, Object>`). A relational DB would require a separate EAV (Entity-Attribute-Value) table or JSONB columns. MongoDB handles this natively.
2. **Synchronous user validation:** We validate the user synchronously (blocking) before saving. This ensures data integrity — no activities for non-existent users. The tradeoff is added latency (~10-50ms for the WebClient call).
3. **Kafka publish after save:** By publishing after the MongoDB save, we guarantee the activity has an `id`. If Kafka is temporarily down, the activity is still saved (no data loss), but the AI recommendation won't be generated until the Kafka message is eventually processed (requires retry/dead-letter queue in the future).
4. **String ID (not Long):** MongoDB's native ObjectId maps cleanly to `String`. Using `Long` with MongoDB causes auto-generation conflicts and is an anti-pattern.
