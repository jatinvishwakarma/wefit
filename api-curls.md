# 🏋️ Wefit API — cURL Reference

> **Base URLs**
> | Service | URL |
> |---|---|
> | UserService | `http://localhost:8081` |
> | ActivityService | `http://localhost:8082` |
> | AiService | `http://localhost:8083` |
> | Eureka Dashboard | `http://localhost:8761` |

---

## 🔐 Auth (`/api/user/auth`) — UserService :8081

### 1. Register a New User
`POST /api/user/auth/register`

> **Required fields:** `firstName`, `lastName`, `userName`, `email`, `password`  
> **Optional fields:** `phoneNumber`, `bio`, `gender`, `dateOfBirth`, `role`  
> **Roles:** `USER` | `ADMIN` | `COACH`

```bash
curl -X POST http://localhost:8081/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "userName": "johndoe",
    "email": "john.doe@example.com",
    "password": "Secret@123",
    "phoneNumber": "+91-9876543210",
    "bio": "Fitness enthusiast",
    "gender": "MALE",
    "dateOfBirth": "1995-04-15",
    "role": "USER"
  }'
```

**Expected Response:** `200 OK` — `UserResponseDto` object with `id`, `userName`, `email`, `role`, timestamps, etc.

---

### 2. Validate User Exists
`GET /api/user/auth/{userId}/validate`

```bash
curl -X GET http://localhost:8081/api/user/auth/1/validate
```

**Expected Response:** `200 OK` — `true` or `false`

---

## 👤 Users (`/api/users`) — UserService :8081

### 3. Get User Profile by Username or Email
`GET /api/users/profile/{identifier}`

> `identifier` can be a **username** or **email address**

```bash
# By username
curl -X GET http://localhost:8081/api/users/profile/johndoe

# By email
curl -X GET "http://localhost:8081/api/users/profile/john.doe@example.com"
```

**Expected Response:** `200 OK` — `UserResponseDto`

---

### 4. Get User by ID
`GET /api/users/{id}`

```bash
curl -X GET http://localhost:8081/api/users/1
```

**Expected Response:** `200 OK` — `UserResponseDto`

---

## 🏃 Activities (`/api/activities`) — ActivityService :8082

### 5. Log a New Activity
`POST /api/activities/add`

> **ActivityType values:** `RUNNING` | `WALKING` | `CYCLING` | `SWIMMING` | `YOGA` | `MEDITATION` | `HIIT` | `STRENGTH_TRAINING` | `CARDIO` | `FLEXIBILITY` | `OTHER`  
> **`startTime` format:** ISO 8601 — `"yyyy-MM-ddTHH:mm:ss"`

```bash
curl -X POST http://localhost:8082/api/activities/add \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "activityType": "RUNNING",
    "durationInMinutes": 45,
    "caloriesBurned": 400,
    "startTime": "2026-06-14T07:00:00",
    "additionalMetrics": {
      "distanceKm": 6.5,
      "avgHeartRate": 145,
      "pace": "6:55 min/km"
    }
  }'
```

**Example with HIIT:**
```bash
curl -X POST http://localhost:8082/api/activities/add \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "activityType": "HIIT",
    "durationInMinutes": 30,
    "caloriesBurned": 350,
    "startTime": "2026-06-14T06:30:00",
    "additionalMetrics": {
      "rounds": 5,
      "restSeconds": 30
    }
  }'
```

**Expected Response:** `200 OK` — `ActivityResponseDto` with generated `id` (MongoDB ObjectId), timestamps, etc.

---

## 🤖 AI Recommendations (`/api/recommendations`) — AiService :8083

### 6. Get All Recommendations for a User
`GET /api/recommendations/user/{userId}`

```bash
curl -X GET http://localhost:8083/api/recommendations/user/1
```

**Expected Response:** `200 OK` — Array of `Recommendation` objects

---

### 7. Get Recommendation for a Specific Activity
`GET /api/recommendations/activity/{activityId}`

> `activityId` is the MongoDB ObjectId string returned when you log an activity (e.g. from endpoint #5)

```bash
curl -X GET http://localhost:8083/api/recommendations/activity/6848f1a2c3d4e5f6a7b8c9d0
```

**Expected Response:** `200 OK` — Single `Recommendation` object

---

## 🔄 End-to-End Test Flow

Follow this sequence to test the full user journey:

```bash
# Step 1 — Register a user
curl -X POST http://localhost:8081/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Smith","userName":"janesmith","email":"jane@wefit.com","password":"Fit@2026","role":"USER"}'

# Step 2 — Fetch the user (use id returned from step 1, e.g. 1)
curl http://localhost:8081/api/users/1

# Step 3 — Validate user exists
curl http://localhost:8081/api/user/auth/1/validate

# Step 4 — Log a running activity for the user
curl -X POST http://localhost:8082/api/activities/add \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"activityType":"RUNNING","durationInMinutes":30,"caloriesBurned":280,"startTime":"2026-06-14T08:00:00","additionalMetrics":{"distanceKm":5}}'

# Step 5 — Get AI recommendations for the user (use activityId from step 4 for endpoint #7)
curl http://localhost:8083/api/recommendations/user/1
```

---

## 📋 Quick Reference Table

| # | Method | Service | Endpoint | Description |
|---|--------|---------|----------|-------------|
| 1 | `POST` | UserService `:8081` | `/api/user/auth/register` | Register a new user |
| 2 | `GET` | UserService `:8081` | `/api/user/auth/{userId}/validate` | Check if user exists |
| 3 | `GET` | UserService `:8081` | `/api/users/profile/{identifier}` | Get profile by username/email |
| 4 | `GET` | UserService `:8081` | `/api/users/{id}` | Get user by ID |
| 5 | `POST` | ActivityService `:8082` | `/api/activities/add` | Log a new activity |
| 6 | `GET` | AiService `:8083` | `/api/recommendations/user/{userId}` | Get all recs for a user |
| 7 | `GET` | AiService `:8083` | `/api/recommendations/activity/{activityId}` | Get rec for an activity |
