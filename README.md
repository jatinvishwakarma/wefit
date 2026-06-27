# 🏋️ Wefit — AI-Powered Social Fitness Platform
<<<<<<< HEAD

=======
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue.svg)](#system-architecture)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.0.6-green.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka-Event--Driven-black.svg)](https://kafka.apache.org/)
[![Gemini](https://img.shields.io/badge/AI-Gemini%202.0-violet.svg)](https://ai.google.dev/)
<<<<<<< HEAD

**Wefit** is an AI-powered fitness ecosystem built using Spring Cloud Microservices. The platform allows users to log workouts, track key physical metrics, and receive instant, personalized training and nutrition recommendations powered by Google's Gemini AI. 

Our ultimate vision is to evolve Wefit into a **vibrant social media fitness platform** where tracking workouts is social, collaborative, and engaging.

---

## 🌟 The Vision: Social Media Fitness Platform
We want to bridge the gap between individual workout logging and social accountability. Wefit is moving towards a community-first social fitness network:

=======
**Wefit** is an AI-powered fitness ecosystem built using Spring Cloud Microservices. The platform allows users to log workouts, track key physical metrics, and receive instant, personalized training and nutrition recommendations powered by Google's Gemini AI. 
Our ultimate vision is to evolve Wefit into a **vibrant social media fitness platform** where tracking workouts is social, collaborative, and engaging.
---
## 🌟 The Vision: Social Media Fitness Platform
We want to bridge the gap between individual workout logging and social accountability. Wefit is moving towards a community-first social fitness network:
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
1. **Fitness Feed**: A home feed where users can share their logged workouts, activities, and AI recommendations with their network.
2. **Social Interaction**: Enable users to follow friends, comment on workout achievements, and cheer each other on using custom reactions (e.g., "Sweat", "Fire", "Strong").
3. **Group Challenges**: Create and join fitness challenges (e.g., "10k steps for 30 days" or "Weekly 50km Cycling") with real-time leaderboards.
4. **AI-Enhanced Sharing**: Users can post their personalized Gemini training plans and nutritional tips directly to their profile or feed to help others.
5. **Coach Integration**: Coaches can create custom workouts, offer feedback, and view their clients' logs directly within the social ecosystem.
<<<<<<< HEAD

---

## 🛠️ What has been done so far

We have built a robust backend foundation using a **Spring Cloud Microservice architecture** with event-driven synchronization:

### 1. Core Platform Infrastructure
*   **Centralized Configuration (`configServer` - Port `8888`)**: Serves environment-specific configuration values to all microservices via a **native filesystem-based** Spring Cloud Config Server. Each service's `application.yml` reads DB credentials, Kafka bootstrap servers, and Eureka zones from a central `config/` folder, eliminating per-service secrets duplication.
*   **Service Discovery (`eureka` - Port `8761`)**: A Eureka registry server where all microservices self-register on startup, enabling load-balanced, dynamic service lookup (`lb://` URIs) without hardcoded host:port pairs.
*   **API Gateway (`apiGateway` - Port `8080`)**: Acts as the single reverse proxy entry point for all client requests. Routes are resolved via Eureka client-side load balancing:
    *   `/api/user/**`, `/api/users/**` ➔ `user-service`
    *   `/api/activity/**`, `/api/activities/**` ➔ `activity-service`
    *   `/api/ai/**`, `/api/recommendations/**` ➔ `ai-service`

### 2. Implemented Services

#### 👤 User Service (`userService` — Port `8081`)
*   **Registration**: `POST /api/user/auth/register` — Accepts a `UserRequestDto` with validation (`@Valid`), persists to PostgreSQL, and returns a `UserResponseDto`.
*   **Validation Endpoint**: `GET /api/user/auth/{userId}/validate` — Returns a boolean, used internally by the Activity Service to confirm a user exists before logging an activity.
*   **Profile Query**: `GET /api/user/{userId}` — Fetches a user's full profile.
*   **Database**: Relational user records stored in **PostgreSQL** (`wefit` database) via Spring Data JPA.
*   **Config Client**: Reads DB URL, credentials, and Eureka zone from Config Server on startup.

#### 🏃 Activity Service (`activityService` — Port `8082`)
*   **Activity Logging**: `POST /api/activity/activities/add` — Accepts an `ActivityRequestDto`, validates the user synchronously via `UserValidationService` (WebClient + Eureka LB), persists to MongoDB, then **publishes the saved activity to the Kafka `activity-events` topic**.
*   **MongoDB ID Fix**: Migrated the `Activity` entity ID from `Long` to `String` (MongoDB ObjectId) to eliminate auto-generation conflicts.
*   **Kafka Producer**: `KafkaTemplate<Object, Object>` sends serialized `Activity` objects to the topic configured via `${kafka.topic.name}`.
*   **WebClient**: Load-balanced `WebClient` configured with Spring Cloud LoadBalancer to resolve `lb://user-service`.
*   **Database**: Flexible activity records stored in **MongoDB** (`WefitActivitydb`).
*   **Config Client**: Reads MongoDB URI, Kafka bootstrap servers, and User Service URL from Config Server.

#### 🤖 AI Service (`aiService` — Port `8083`)
*   **Kafka Consumer**: `ActivityMessageListener` listens on the `activity-events` topic. On each message, it calls `ActivityAiService` to generate a structured AI recommendation.
*   **Gemini Integration**: `GeminiService` calls the **Google Gemini API** (`gemini-2.0-flash`) with a structured prompt. The prompt requests a JSON response with four sections: `analysis`, `improvements`, `suggestions`, and `safety`.
*   **AI Response Parsing**: `ActivityAiService` deserializes the raw Gemini JSON, strips markdown fences, and maps it into a `Recommendation` entity with typed lists for improvements, suggestions, and safety precautions.
*   **Fallback**: On Gemini API failure, a default fallback recommendation is saved so no activity goes unprocessed.
*   **Fetch Endpoints**:
    *   `GET /api/ai/recommendations/user/{userId}` — All recommendations for a user.
    *   `GET /api/ai/recommendations/activity/{activityId}` — Recommendation for a specific activity.
*   **Database**: AI recommendations stored in **MongoDB** (`AiRecommendationsdb`).
*   **Config Client**: Reads MongoDB URI, Kafka bootstrap servers, and Gemini API key from Config Server.

### 3. Infrastructure & Developer Automation
*   **`start-services.bat`**: Windows batch script that reads `.env` for environment variables and sequentially starts all services (Eureka → Config Server → User Service → Activity Service → AI Service) each in its own command window — includes a delay between starts to respect dependency order.
*   **`start_services.py`**: Cross-platform Python alternative that opens each service in a separate PowerShell window while injecting `.env` variables, allowing clean local development without manual export of secrets.
*   **`api-curls.md`**: Complete reference of `curl` commands covering the full end-to-end test journey (register user → log activity → fetch AI recommendation) plus raw per-service API examples.

---

## 📐 System Architecture

The following diagram illustrates how the components interact:

```mermaid
graph TD
    Client[Mobile/Web Client] -->|HTTP Requests| Gateway[API Gateway :8080]
=======
---
## 🛠️ What has been done so far
We have built a robust backend foundation using a **Spring Cloud Microservice architecture** with event-driven synchronization:
### 1. Core Platform Infrastructure
*   **Centralized Configuration (`configServer` - Port `8888`)**: Serves environment-specific configuration values to all microservices dynamically.
*   **Service Discovery (`eureka` - Port `8761`)**: A registry server where all microservices register themselves, enabling dynamic service lookup and load balancing.
*   **API Gateway (`apiGateway` - Port `8085`)**: Act as a reverse proxy, routing incoming client requests to their appropriate services via Eureka load balancing (`lb://`).
    *   `/api/user/**` ➔ `user-service`
    *   `/api/activity/**` ➔ `activity-service`
    *   `/api/ai/**` ➔ `ai-service`
### 2. Implemented Services
*   **User Service (`userService` - Port `8081`)**
    *   Handles secure user registration, profile queries, and validation.
    *   **Database**: Relational user details stored in **PostgreSQL** (`wefit` database).
*   **Activity Service (`activityService` - Port `8082`)**
    *   Logs user activities (Running, Cycling, HIIT, Yoga, Meditation, etc.) with custom metrics (pace, calories, duration).
    *   Communicates synchronously with `user-service` via WebClient to validate the user before logging.
    *   **Database**: Flexible activity structures stored in **MongoDB** (`WefitActivitydb`).
    *   **Kafka Producer**: Emits an event to the `activity-events` topic on successful activity logging.
*   **AI Service (`aiService` - Port `8083`)**
    *   **Kafka Consumer**: Listens for logged activities on the `activity-events` topic in real-time.
    *   **Gemini Integration**: Calls the Google Gemini API (`gemini-2.0-flash` or `gemini-flash-latest`) to generate customized feedback, recovery tips, and nutritional suggestions for that activity.
    *   **Database**: Stores generated AI recommendations in **MongoDB** (`AiRecommendationsdb`).
    *   Provides API endpoints to fetch recommendations by user or specific activity.
---
## 📐 System Architecture
The following diagram illustrates how the components interact:
```mermaid
graph TD
    Client[Mobile/Web Client] -->|HTTP Requests| Gateway[API Gateway :8085]
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
    
    Gateway -->|Routes| UserService[User Service :8081]
    Gateway -->|Routes| ActivityService[Activity Service :8082]
    Gateway -->|Routes| AiService[AI Service :8083]
    
    %% Service Discovery & Config %%
    UserService & ActivityService & AiService -->|Register/Discover| Eureka[Eureka Server :8761]
    UserService & ActivityService & AiService -->|Fetch Config| ConfigServer[Config Server :8888]
    
    %% Databases %%
    UserService -->|JPA| PostgreSQL[(PostgreSQL)]
    ActivityService -->|NoSQL| MongoActivity[(MongoDB: WefitActivitydb)]
    AiService -->|NoSQL| MongoAi[(MongoDB: AiRecommendationsdb)]
    
    %% Event Driven %%
    ActivityService -->|Publish 'activity-events'| Kafka[[Apache Kafka :9092]]
    Kafka -->|Consume| AiService
    
    %% External Integration %%
    AiService -->|Generate Advice| Gemini[Gemini API]
    
    %% Sync Validation %%
    ActivityService -.->|REST / Validate User| UserService
```
<<<<<<< HEAD

---

## 🚀 Getting Started

=======
---
## 🚀 Getting Started
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
### 📋 Prerequisites
Make sure you have the following installed and running locally:
*   **Java 21**
*   **Maven**
*   **PostgreSQL** (with a database named `wefit` created)
*   **MongoDB** (running on port `27017`)
*   **Apache Kafka** (running on port `9092` with Zookeeper/KRaft)
<<<<<<< HEAD

=======
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
### 🔑 Environment Variables
Create or update the `.env` file in the root directory to match your environment settings:
```ini
# Database Configs
DB_URL_USER_SERVICE=jdbc:postgresql://localhost:5432/wefit
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
<<<<<<< HEAD

# MongoDB Configs
MONGODB_URI_ACTIVITY_SERVICE=mongodb://localhost:27017/WefitActivitydb
MONGODB_URI_AI_SERVICE=mongodb://localhost:27017/AiRecommendationsdb

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Gemini API Key
GEMINI_API_KEY=your_gemini_api_key
```

### ⚡ Starting the Services
You can spin up all the services sequentially with a single command.

=======
# MongoDB Configs
MONGODB_URI_ACTIVITY_SERVICE=mongodb://localhost:27017/WefitActivitydb
MONGODB_URI_AI_SERVICE=mongodb://localhost:27017/AiRecommendationsdb
# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
# Gemini API Key
GEMINI_API_KEY=your_gemini_api_key
```
### ⚡ Starting the Services
You can spin up all the services sequentially with a single command.
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
#### On Windows (Batch Script)
Run the script to launch each service in its own command window:
```bash
start-services.bat
```
<<<<<<< HEAD

=======
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
#### Via Python Script
Alternatively, run the python script which opens the services in separate PowerShell windows while preserving variables from the `.env` file:
```bash
python start_services.py
```
<<<<<<< HEAD

---

## 🧪 Testing the APIs

For a complete set of cURL requests, refer to [api-curls.md](file:///c:/Wefit/api-curls.md).

Here is the quick **End-to-End Test Journey**:

1.  **Register a User** via API Gateway:
    ```bash
    curl -X POST http://localhost:8080/api/user/auth/register \
=======
---
## 🧪 Testing the APIs
For a complete set of cURL requests, refer to [api-curls.md](file:///c:/Wefit/api-curls.md).
Here is the quick **End-to-End Test Journey**:
1.  **Register a User** via API Gateway:
    ```bash
    curl -X POST http://localhost:8085/api/user/auth/register \
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
      -H "Content-Type: application/json" \
      -d '{"firstName":"Jane","lastName":"Smith","userName":"janesmith","email":"jane@wefit.com","password":"Fit@2026","role":"USER"}'
    ```
2.  **Log an Activity**:
    ```bash
<<<<<<< HEAD
    curl -X POST http://localhost:8080/api/activities/add \
=======
    curl -X POST http://localhost:8085/api/activity/activities/add \
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
      -H "Content-Type: application/json" \
      -d '{"userId":1,"activityType":"RUNNING","durationInMinutes":30,"caloriesBurned":280,"startTime":"2026-06-20T08:00:00","additionalMetrics":{"distanceKm":5}}'
    ```
3.  **Fetch AI Recommendation** generated asynchronously:
    ```bash
<<<<<<< HEAD
    curl http://localhost:8080/api/recommendations/user/1
    ```

---

=======
    curl http://localhost:8085/api/ai/recommendations/user/1
    ```
---
>>>>>>> 07359a714b9fb6c1af0d1c9d1fd963fca4cc23f1
## 🗺️ Roadmap to a Social Media Fitness Platform
Here are the next steps to transform this architecture into a social experience:
*   [ ] **Feed Service**: A new microservice dedicated to timeline generation, managing user posts, likes, comments, and activity shares.
*   [ ] **Relationship Service**: Dedicated to user follow networks (following/followers graph database or JPA layout).
*   [ ] **Real-time Notifications**: A WebSocket or SSE server to alert users of friends' activities, comments, or feed likes.
*   [ ] **Gamification & Leaderboards**: Scheduled batch processing using Spring Batch or Spark to process user logs and compute leaderboard scores.
*   [ ] **Mobile/Frontend Client**: A responsive interface built using Next.js or React Native to visually showcase the feeds, workout tracking, and AI tips.
