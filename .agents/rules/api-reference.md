# WeFit — API Reference

## UserService (Port 8081)

### Authentication & Validation

#### POST `/api/user/auth/register`

Register a new user.

**Controller**: [AuthController.java](file:///c:/Wefit/userService/src/main/java/com/wefit/userService/controller/AuthController.java)

**Request Body** (`UserRequestDto`):
```json
{
  "firstName": "John",          // required
  "lastName": "Doe",            // required
  "userName": "johndoe",        // required, unique
  "email": "john@example.com",  // required, unique, valid email
  "password": "secret123",      // required
  "phoneNumber": "+1234567890", // optional
  "bio": "Fitness enthusiast",  // optional
  "gender": "Male",             // optional
  "dateOfBirth": "1990-01-15",  // optional (String)
  "role": "USER"                // optional, defaults to USER. Values: USER, ADMIN, COACH
}
```

**Response** (`UserResponseDto`, 200 OK):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "userName": "johndoe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "bio": "Fitness enthusiast",
  "gender": "Male",
  "dateOfBirth": "1990-01-15",
  "role": "USER",
  "profilePicUrl": null,
  "createdDateTime": "2026-06-11T22:30:00",
  "updadatedDateTime": "2026-06-11T22:30:00"
}
```

**Errors**:
- `500` — "Email already exists" (RuntimeException, no proper error response)

---

#### GET `/api/user/auth/{userId}/validate`

Validate if a user ID exists. Used internally by ActivityService.

**Controller**: [AuthController.java](file:///c:/Wefit/userService/src/main/java/com/wefit/userService/controller/AuthController.java)

**Path Parameters**:
- `userId` (Long) — The user ID to validate

**Response** (200 OK):
```json
true   // or false
```

---

### User Profile

#### GET `/api/users/profile/{identifier}`

Get user profile by username OR email.

**Controller**: [UserController.java](file:///c:/Wefit/userService/src/main/java/com/wefit/userService/controller/UserController.java)

**Path Parameters**:
- `identifier` (String) — Username or email address

**Response** (`UserResponseDto`, 200 OK): Same structure as registration response.

**Errors**:
- `500` — "User not found with username or email: {identifier}"

---

#### GET `/api/users/{id}`

Get user by numeric ID.

**Controller**: [UserController.java](file:///c:/Wefit/userService/src/main/java/com/wefit/userService/controller/UserController.java)

**Path Parameters**:
- `id` (Long) — User ID

**Response** (`UserResponseDto`, 200 OK): Same structure as registration response.

**Errors**:
- `500` — "User not found with id: {id}"

---

## ActivityService (Port 8082)

#### POST `/api/activities/add`

Log a new fitness activity.

**Controller**: [ActivityController.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/controller/ActivityController.java)

**Side Effects**:
1. Validates user via UserService REST call
2. Saves activity to MongoDB
3. Publishes activity event to Kafka topic `activity-events`

**Request Body** (`ActivityRequestDto`):
```json
{
  "activityType": "RUNNING",
  "durationInMinutes": 45,
  "userId": 1,
  "caloriesBurned": 350,
  "startTime": "2026-06-11T07:00:00",
  "additionalMetrics": {
    "distance_km": 5.2,
    "avg_heart_rate": 145,
    "steps": 6500
  }
}
```

**Valid `activityType` values**: `RUNNING`, `WALKING`, `CYCLING`, `SWIMMING`, `YOGA`, `MEDITATION`, `HIIT`, `STRENGTH_TRAINING`, `CARDIO`, `FLEXIBILITY`, `OTHER`

**Response** (`ActivityResponseDto`, 200 OK):
```json
{
  "id": "665f...",
  "activityType": "RUNNING",
  "durationInMinutes": 45,
  "userId": 1,
  "caloriesBurned": 350,
  "startTime": "2026-06-11T07:00:00",
  "additionalMetrics": {
    "distance_km": 5.2,
    "avg_heart_rate": 145,
    "steps": 6500
  },
  "createdAt": "2026-06-11T22:30:00",
  "updatedAt": "2026-06-11T22:30:00"
}
```

**Errors**:
- `500` — "Invalid user" (if userId doesn't exist or UserService unreachable)

---

## AiService (Port 8083)

> **Note**: The `@RequestMapping` in `RecommendationController` is `"api/recommendations"` (missing leading `/`). Spring resolves it the same way, but this is inconsistent with other services.

#### GET `api/recommendations/user/{userId}`

Get latest 5 recommendations for a user.

**Controller**: [RecommendationController.java](file:///c:/Wefit/aiService/src/main/java/com/wefit/aiService/controller/RecommendationController.java)

**Path Parameters**:
- `userId` (Long) — User ID

**Response** (`List<Recommendation>`, 200 OK):
```json
[
  {
    "id": "665f...",
    "userId": 1,
    "activityId": "665e...",
    "recommendation": "Great job on your RUNNING session!",
    "improvements": ["Try to maintain a steady pace.", "Keep hydrated."],
    "suggestions": ["Increase duration by 5 minutes next time."],
    "safetyPrecautions": ["Stretch before and after the session."],
    "createdAt": "2026-06-11T22:30:00"
  }
]
```

> **Note**: Returns entity directly, not a DTO. This is a design inconsistency compared to other services.

---

#### GET `api/recommendations/activity/{activityId}`

Get recommendation for a specific activity.

**Controller**: [RecommendationController.java](file:///c:/Wefit/aiService/src/main/java/com/wefit/aiService/controller/RecommendationController.java)

**Path Parameters**:
- `activityId` (String) — MongoDB activity document ID

**Response** (`Recommendation`, 200 OK): Single recommendation object (same structure as above).

---

## Internal Service-to-Service API

### ActivityService → UserService

```
GET http://user-service/api/user/auth/{userId}/validate
```

- Called by [UserValidationService.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/service/UserValidationService.java)
- Uses LoadBalanced WebClient (Eureka resolution)
- Returns `Boolean` — `true` if user exists
- Wrapped in try/catch — returns `false` on any error (service down, network issues, etc.)

### ActivityService → AiService (via Kafka)

- **Topic**: `activity-events`
- **Payload**: `Activity` entity serialized as JSON
- **Direction**: ActivityService produces → AiService consumes
