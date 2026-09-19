# WeFit — Workflows

## Core Business Workflows

### 1. User Registration Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant US as UserService
    participant UR as UserRepository
    participant PG as PostgreSQL

    C->>AC: POST /api/user/auth/register (UserRequestDto)
    AC->>US: registerUser(dto)
    US->>UR: existsByEmail(email)
    UR->>PG: SELECT EXISTS
    PG-->>UR: true/false
    alt Email exists
        US-->>AC: throw RuntimeException("Email already exists")
        AC-->>C: 500 Internal Server Error
    else Email is new
        US->>US: Build User entity with Builder
        US->>UR: save(user)
        UR->>PG: INSERT INTO users
        PG-->>UR: saved entity
        UR-->>US: User
        US->>US: convertToResponseDto(user)
        US-->>AC: UserResponseDto
        AC-->>C: 200 OK (UserResponseDto)
    end
```

**Key files**:
- [AuthController.java](file:///c:/Wefit/userService/src/main/java/com/wefit/userService/controller/AuthController.java)
- [UserService.java](file:///c:/Wefit/userService/src/main/java/com/wefit/userService/service/UserService.java)

---

### 2. Activity Logging Flow (with Kafka)

```mermaid
sequenceDiagram
    participant C as Client
    participant ACtrl as ActivityController
    participant AS as ActivityService
    participant UVS as UserValidationService
    participant WC as WebClient
    participant US as UserService (8081)
    participant AR as ActivityRepository
    participant MDB as MongoDB
    participant K as Kafka
    participant AML as ActivityMessageListener
    participant RS as RecommendationService
    participant RR as RecommendationRepo
    participant MDB2 as MongoDB (AI)

    C->>ACtrl: POST /api/activities/add (ActivityRequestDto)
    ACtrl->>AS: addActivity(dto)
    AS->>UVS: validateUser(userId)
    UVS->>WC: GET http://user-service/api/user/auth/{id}/validate
    WC->>US: HTTP GET (via Eureka)
    US-->>WC: true/false
    WC-->>UVS: Boolean

    alt User invalid or service down
        UVS-->>AS: false
        AS-->>ACtrl: throw RuntimeException("Invalid user")
        ACtrl-->>C: 500 Internal Server Error
    else User valid
        UVS-->>AS: true
        AS->>AS: Activity.fromEntity(dto)
        AS->>AR: save(activity)
        AR->>MDB: Insert document
        MDB-->>AR: saved Activity
        AS->>AS: ActivityResponseDto.toDto(saved)
        AS-->>ACtrl: ActivityResponseDto
        ACtrl-->>C: 200 OK (ActivityResponseDto)

        Note over K: Kafka event published (TODO: not yet implemented in code)
    end

    Note right of K: Async flow (separate thread)
    K->>AML: Consume activity-events
    AML->>RS: generateRecommendation(activity)
    RS->>RS: Build Recommendation (hardcoded text)
    RS->>RR: save(recommendation)
    RR->>MDB2: Insert document
```

> **Important observation**: The ActivityService has Kafka producer dependencies and configuration, but the actual `KafkaTemplate.send()` call is **not yet implemented** in `ActivityService.java`. The Kafka producer publish step is missing from the current code. The consumer (AiService) is fully wired.

**Key files**:
- [ActivityController.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/controller/ActivityController.java)
- [ActivityService.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/service/ActivityService.java)
- [UserValidationService.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/service/UserValidationService.java)
- [ActivityMessageListener.java](file:///c:/Wefit/aiService/src/main/java/com/wefit/aiService/service/ActivityMessageListener.java)
- [RecommendationService.java](file:///c:/Wefit/aiService/src/main/java/com/wefit/aiService/service/RecommendationService.java)

---

### 3. Recommendation Retrieval Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant RC as RecommendationController
    participant RS as RecommendationService
    participant RR as RecommendationRepository
    participant MDB as MongoDB

    C->>RC: GET api/recommendations/user/{userId}
    RC->>RS: getUserRecommendations(userId)
    RS->>RR: findTop5ByUserIdOrderByCreatedAtDesc(userId)
    RR->>MDB: Query ai_recommendations
    MDB-->>RR: List<Recommendation>
    RR-->>RS: List<Recommendation>
    RS-->>RC: List<Recommendation>
    RC-->>C: 200 OK (List<Recommendation>)
```

---

### 4. User Profile Lookup Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant UC as UserController
    participant US as UserService
    participant UR as UserRepository
    participant PG as PostgreSQL

    C->>UC: GET /api/users/profile/{identifier}
    UC->>US: getUserProfile(identifier)
    US->>UR: findByUserNameOrEmail(identifier, identifier)
    UR->>PG: SELECT WHERE user_name = ? OR email = ?
    PG-->>UR: Optional<User>
    alt User not found
        US-->>UC: throw RuntimeException
        UC-->>C: 500 Internal Server Error
    else User found
        US->>US: convertToResponseDto(user)
        US-->>UC: UserResponseDto
        UC-->>C: 200 OK (UserResponseDto)
    end
```

---

## Startup Workflow

Two scripts are provided for starting all services in the correct order:

### Windows Batch ([start-services.bat](file:///c:/Wefit/start-services.bat))
### Python Cross-Platform ([start_services.py](file:///c:/Wefit/start_services.py))

```
1. Start Eureka Server     → Wait 30 seconds
2. Start UserService       → Wait 15 seconds
3. Start ActivityService   → Wait 15 seconds
4. Start AiService         → No wait (last service)
```

Each service is started in its own console window via `mvnw.cmd spring-boot:run`.

## Development Workflow

1. Ensure infrastructure is running: PostgreSQL (5432), MongoDB (27017), Kafka (9092)
2. Run `start-services.bat` or `python start_services.py` from the project root
3. Verify Eureka dashboard at `http://localhost:8761`
4. Test APIs via REST client (Postman, curl, etc.)
