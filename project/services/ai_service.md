# 🤖 AI Service — Gemini AI Integration, Personal Coach & Analytics

> **Module:** `aiService/`
> **Spring Name:** `ai-service`
> **Port:** `8083`
> **Database:** MongoDB (`AiRecommendationsdb`)
> **Last Updated:** 2026-10-02

---

## Purpose

The AI Service is the **intelligence layer** of Wefit. It:

1. **Consumes** activity events from the Kafka `activity-events` topic asynchronously.
2. **Generates** personalised fitness recommendations using Google's Gemini 2.0 Flash API.
3. **Provides** a conversational AI Personal Coach with persistent session history.
4. **Generates** structured, multi-day Workout Plans on demand.
5. **Generates** personalised Nutrition Plans using BMR/TDEE calculations.
6. **Produces** Progress Analytics reports with burnout risk detection.
7. **Enforces** AI Safety via `SafetyEvaluationService` on all prompts and outputs.
8. **Persists** all AI output to MongoDB.
9. **Serves** all data via REST endpoints at `/api/v1/ai/**`.

---

## Directory Structure

```
aiService/
├── src/main/java/com/wefit/aiService/
│   ├── AiServiceApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java                    ← Kafka consumer configuration
│   │   └── WebClientConfig.java                ← WebClient builder bean
│   ├── controller/
│   │   ├── AiCoachController.java              ← POST /api/v1/ai/coach/{userId}/chat
│   │   ├── AiSafetyController.java             ← POST /api/v1/ai/safety/validate-prompt
│   │   ├── AnalyticsController.java            ← POST /api/v1/ai/analytics/{userId}/generate
│   │   ├── NutritionPlanController.java        ← POST /api/v1/ai/nutrition-plans/{userId}/generate
│   │   ├── RecommendationController.java       ← GET  /api/v1/recommendations/{userId}
│   │   └── WorkoutPlanController.java          ← POST /api/v1/ai/workout-plans/{userId}/generate
│   ├── dto/
│   │   ├── AnalyticsReportRequest.java
│   │   ├── AnalyticsReportResponse.java
│   │   ├── ChatRequest.java
│   │   ├── ChatResponse.java
│   │   ├── NutritionPlanRequest.java
│   │   ├── NutritionPlanResponse.java
│   │   ├── RecommendationResponseDto.java
│   │   ├── WorkoutPlanRequest.java
│   │   └── WorkoutPlanResponse.java
│   ├── entities/
│   │   ├── Activity.java                       ← Kafka payload mirror
│   │   ├── ActivityType.java                   ← Enum
│   │   ├── AnalyticsReport.java                ← MongoDB: analytics + burnout risk
│   │   ├── ChatSession.java                    ← MongoDB: conversational history
│   │   ├── NutritionPlan.java                  ← MongoDB: nutrition plans
│   │   ├── Recommendation.java                 ← MongoDB: AI recommendations
│   │   └── WorkoutPlan.java                    ← MongoDB: structured workout schedules
│   ├── repositories/
│   │   ├── AnalyticsReportRepository.java
│   │   ├── ChatSessionRepository.java
│   │   ├── NutritionPlanRepository.java
│   │   ├── RecommendationRepository.java
│   │   └── WorkoutPlanRepository.java
│   └── service/
│       ├── ActivityAiService.java              ← Kafka consumer: activity → recommendation
│       ├── ActivityMessageListener.java        ← Kafka consumer listener
│       ├── AiCoachService.java                 ← Conversational AI coach with session state
│       ├── AnalyticsService.java               ← Progress analytics + burnout risk
│       ├── GeminiService.java                  ← HTTP client for Gemini API
│       ├── NutritionPlanService.java           ← BMR/TDEE + Gemini meal suggestions
│       ├── RecommendationService.java          ← CRUD for recommendations
│       ├── SafetyEvaluationService.java        ← Pre/post safety checks on all AI I/O
│       └── WorkoutPlanService.java             ← JSON-structured workout plan generation
└── pom.xml
```

---

## API Endpoints

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| `GET` | `/api/v1/recommendations/{userId}` | Get all recommendations for a user | — | `List<RecommendationResponseDto>` |
| `POST` | `/api/v1/recommendations/{id}/rate` | Rate a recommendation | `{rating, feedback}` | `RecommendationResponseDto` |
| `POST` | `/api/v1/ai/coach/{userId}/chat` | Send a message to the AI Personal Coach | `ChatRequest {message}` | `ChatResponse {response, isSafe}` |
| `POST` | `/api/v1/ai/workout-plans/{userId}/generate` | Generate a personalised workout plan | `WorkoutPlanRequest` | `WorkoutPlanResponse` |
| `GET` | `/api/v1/ai/workout-plans/{userId}` | Get past workout plans | — | `List<WorkoutPlanResponse>` |
| `POST` | `/api/v1/ai/nutrition-plans/{userId}/generate` | Generate nutrition plan with macros | `NutritionPlanRequest` | `NutritionPlanResponse` |
| `GET` | `/api/v1/ai/nutrition-plans/{userId}` | Get past nutrition plans | — | `List<NutritionPlanResponse>` |
| `POST` | `/api/v1/ai/analytics/{userId}/generate` | Generate progress analytics report | `AnalyticsReportRequest` | `AnalyticsReportResponse` |
| `GET` | `/api/v1/ai/analytics/{userId}` | Get past analytics reports | — | `List<AnalyticsReportResponse>` |
| `POST` | `/api/v1/ai/safety/validate-prompt` | Validate a prompt for safety | `{prompt}` | `{safe, reason}` |

---

## MongoDB Schemas

### `recommendations` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | Long | FK to User Service |
| `activityId` | String | FK to Activity |
| `analysis` | String | General activity analysis |
| `improvements` | String | Improvement suggestions |
| `suggestions` | String | Next workout suggestions |
| `safetyNotes` | String | Safety warnings from AI |
| `rating` | Integer | User rating (1-5) |
| `feedback` | String | User feedback text |
| `createdAt` | LocalDateTime | Created timestamp |

### `chat_sessions` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | Long | FK to User Service |
| `messages` | List\<ChatMessage\> | Full conversation history |
| `messages[].role` | String | `"user"` or `"model"` |
| `messages[].content` | String | Message text |
| `messages[].timestamp` | LocalDateTime | Message timestamp |
| `createdAt` | LocalDateTime | Session created |
| `updatedAt` | LocalDateTime | Session last updated |

> **Design Decision**: Only the last 10 messages are passed to Gemini per request to control token costs. Full history is still persisted in MongoDB for UX continuity.

### `workout_plans` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | Long | FK to User Service |
| `goal` | String | e.g. "Weight Loss", "Muscle Gain" |
| `fitnessLevel` | String | "Beginner" / "Intermediate" / "Advanced" |
| `daysPerWeek` | Integer | Target workout days |
| `schedule` | List\<DailyWorkout\> | Per-day exercise breakdown |
| `schedule[].day` | int | Day number |
| `schedule[].focus` | String | e.g. "Upper Body" |
| `schedule[].exercises` | List\<Exercise\> | Exercise list |
| `schedule[].exercises[].name` | String | Exercise name |
| `schedule[].exercises[].sets` | String | e.g. "3" |
| `schedule[].exercises[].reps` | String | e.g. "10-15" |
| `schedule[].exercises[].rest` | String | e.g. "60s" |
| `createdAt` | LocalDateTime | Created timestamp |

### `nutrition_plans` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | Long | FK to User Service |
| `age` | Integer | User age |
| `gender` | String | MALE / FEMALE |
| `weightKg` | Double | Weight in kg |
| `heightCm` | Double | Height in cm |
| `activityLevel` | String | SEDENTARY → EXTRA_ACTIVE |
| `goal` | String | LOSE_WEIGHT / MAINTAIN / GAIN_WEIGHT |
| `targetCalories` | Double | Computed via Mifflin-St Jeor + TDEE |
| `proteinGrams` | Double | 30% of calories / 4 |
| `carbsGrams` | Double | 40% of calories / 4 |
| `fatGrams` | Double | 30% of calories / 9 |
| `mealSuggestions` | List\<MealSuggestion\> | 4 AI-generated meals |
| `createdAt` | LocalDateTime | Created timestamp |

> **Design Decision**: Calorie calculation is deterministic (Mifflin-St Jeor equation). Only the meal suggestions are AI-generated, ensuring reproducible macro targets regardless of AI variation.

### `analytics_reports` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | Long | FK to User Service |
| `totalActivities` | Integer | Total activities tracked |
| `totalDurationMinutes` | Integer | Total duration |
| `totalCaloriesBurned` | Integer | Total calories |
| `burnoutRiskScore` | Double | 0.0–10.0 score from Gemini |
| `burnoutWarning` | String | Human-readable warning |
| `progressAnalysis` | String | Progress summary |
| `predictiveInsights` | String | Future trajectory prediction |
| `createdAt` | LocalDateTime | Created timestamp |

---

## Key Services

### `SafetyEvaluationService` (SCRUM-46)
- `isPromptSafe(String prompt)` — blocks prompts containing medical/harmful keywords
- `isOutputSafe(String output)` — filters AI responses mentioning specific medical conditions
- `getUnsafePromptFallbackMessage()` — returns standardised safe fallback
- Applied to ALL AI endpoints before calling Gemini

### `AiCoachService` (SCRUM-42)
- Maintains `ChatSession` per user in MongoDB
- Sends last 10 messages to Gemini with a system instruction: "You are an AI Personal Coach for the Wefit app"
- Safety-checks both input and output

### `WorkoutPlanService` (SCRUM-43)
- Sends structured JSON-format prompt to Gemini
- Parses Gemini JSON output (with markdown wrapper stripping fallback)
- Saves structured `WorkoutPlan` document to MongoDB

### `NutritionPlanService` (SCRUM-44)
- **Deterministically** calculates BMR using Mifflin-St Jeor equation
- Calculates TDEE using activity multipliers
- Adjusts for goal (−500 kcal lose / +500 kcal gain)
- Passes final macros to Gemini for meal suggestions only

### `AnalyticsService` (SCRUM-45)
- Sends activity summary to Gemini for burnout risk scoring (0–10)
- Returns structured JSON with `burnoutRiskScore`, `burnoutWarning`, `progressAnalysis`, `predictiveInsights`

### `GeminiService`
- Exposes `getRecommendations(String prompt)` for single-turn prompts
- Exposes `getChatResponse(List<Map> contents)` for multi-turn chat (with `systemInstruction`)
- Both delegate to private `callGemini(Map requestBody)` via Spring `RestClient`

---

## Kafka Configuration

| Property | Value |
|----------|-------|
| **Consumer Topic** | `activity-events` |
| **Consumer Group** | `ai-recommendation-group` |
| **Deserialization** | `JsonDeserializer` for `Activity` objects |

---

## Design Decisions

1. **Safety-first architecture**: `SafetyEvaluationService` is not optional — it wraps every Gemini call. The service degrades gracefully (returns safe fallback) rather than throwing errors.
2. **Keyword-based safety (not AI-based)**: Safety checks are deterministic regex/keyword matches rather than a second LLM call, minimising latency and cost.
3. **Deterministic + AI hybrid for nutrition**: Macros are calculated deterministically; only meal suggestions use AI, preventing calorie drift across regenerations.
4. **Session rolling window**: Chat context limited to 10 messages to control Gemini token costs while maintaining conversational coherence.
5. **JSON prompt engineering**: Workout and nutrition prompts explicitly request raw JSON (no markdown) with exact schema. A markdown-stripping fallback handles cases where Gemini wraps output in fences.
