# WeFit — Changelog

All notable changes to this project are documented in this file.

---

## [2026-10-03] — Gamification, AI Coach, DevOps, Web/Mobile Clients (Phase 5 & 6)

### Added
- **aiService**: Completed `SCRUM-10` by linking `ai-service` to MongoDB and fixing container networking.
- **gamificationService**: Created `gamification-service` (SCRUM-11) to handle Leveling, XP, Streaks, Challenges, and Badges using PostgreSQL. Added `V1__Init_Gamification.sql` Flyway migration.
- **wefit-web**: Updated `Dashboard.jsx` (SCRUM-12) to dynamically fetch User Profile, AI insights, Leaderboard, and Gamification stats via `axios` and `@tanstack/react-query`.
- **admin-dashboard**: Scaffolded Next.js Admin Dashboard application repository (SCRUM-12).
- **mobile-app**: Scaffolded Expo React Native mobile application repository (SCRUM-12).
- **DevOps**: Completed `SCRUM-13`. Updated `.github/workflows/ci.yml` matrix with the new microservices (`gamificationService`, `feedService`, `mediaService`, etc.).
- **DevOps**: Appended new Kubernetes Deployments and Services for all missing microservices to `k8s/wefit-stack.yaml`.
- **DevOps**: Authored `OPERATIONS.md` defining operational runbooks, disaster recovery procedures, and Service Level Objectives (SLOs).

### Documentation Updated
- `.agents/rules/changelog.md` — Logged phase 5 & 6 completions.
- `OPERATIONS.md` — Created for system administration procedures.


## [2026-10-03] — Backend Stabilization & Bug Fixes

### Fixed
- **mediaService**, **moderationService**, **relationshipService**: Downgraded Spring Boot from 4.0.6 to 3.2.3 and Spring Cloud from 2025.1.1 to 2023.0.0 to fix `NoSuchMethodError` with AWS S3 SDK and resolve Kafka auto-configuration conflicts.
- **relationshipService**: Reverted `KafkaTemplate` injection from `<Object, Object>` to `<String, String>` to align with Spring Boot 3 default auto-configuration.
- **relationshipService**: Removed `flyway-database-postgresql` dependency (only needed for Flyway 10/Spring Boot 4).
- **configServer**: Updated `relationship-service.yml` hibernate `ddl-auto` from `validate` to `update` so tables are correctly created on startup.
- **mediaService**: Updated `spring-boot-starter-webmvc` to `spring-boot-starter-web` for correct HTTP support.
- **Environment**: Fixed dangling Java process issues blocking port 8085 (`relationshipService`) by implementing clean script restarts.

### Documentation Updated
- `.agents/rules/changelog.md` — Logged stabilization fixes.
- `.agents/rules/troubleshooting.md` — Documented Spring Boot 4 to 3 downgrade and Kafka generic type injection GOTCHA.

---

### Added
- **aiService**: `ChatSession` entity, `AiCoachService` (rolling 10-msg context), `AiCoachController` — `POST /api/v1/ai/coach/{userId}/chat`.
- **aiService**: `WorkoutPlan` entity, `WorkoutPlanService` (JSON-structured Gemini prompt), `WorkoutPlanController` — generate + list endpoints.
- **aiService**: `NutritionPlan` entity, `NutritionPlanService` (Mifflin-St Jeor BMR/TDEE + Gemini meals), `NutritionPlanController`.
- **aiService**: `AnalyticsReport` entity, `AnalyticsService` (burnout risk 0-10), `AnalyticsController`.
- **aiService**: `SafetyEvaluationService` (SCRUM-46) — keyword safety on all Gemini I/O; `AiSafetyController`.
- **activityService**: `Challenge` + `UserChallenge` entities, `ChallengeService` (create/join/progress/complete), `ChallengeController`.
- **activityService**: Kafka producer on `challenge-completed` topic when challenge is auto-completed.
- **configServer**: Added `/api/v1/challenges/**` route in `api-gateway.yml` → `activity-service`.

### Fixed
- **activityService**: Removed duplicate `spring-boot-starter-webmvc-test` in `pom.xml`.

### Infrastructure
- **Root `.gitignore`**: Now excludes `certs/`, `.gemini/`, `.gsd/`, `scratch/`, `model_capabilities.yaml`, `target/`, `node_modules/`.
- **GitHub**: Pushed PR `feature/SCRUM-42-47-ai-coach-nutrition-analytics-challenges`.

### Documentation Updated
- `project/services/ai_service.md` — Full rewrite; all new entities, endpoints, schemas, design decisions.
- `project/services/activity_service.md` — Challenge Engine section added.
- `project/services/moderation_service.md` — Created (new service).
- `project/services/relationship_service.md` — Created (new service).
- `project/services/media_service.md` — Created (new service).

---

## [2026-10-01] — SCRUM-38 & SCRUM-40

### Added
- **relationshipService**: Created new microservice for social graph management (followers, following, blocking, friend requests).
- **relationshipService**: Added PostgreSQL migration, JPA entities, and endpoints for follow flow.
- **relationshipService**: Integrated Kafka to publish follow/unfollow events.
- **mediaService**: Created new microservice for handling media uploads.
- **mediaService**: Integrated with AWS S3 SDK to generate pre-signed URLs for direct-to-S3 uploads.
- **docker-compose**: Added Minio service to simulate S3 locally. Added `relationship-service`, `media-service`, and `minio` to `docker-compose.yml`.
- **configServer**: Created `application.yml` and `media-service.yml` for the new services.
- **apiGateway**: Added routing rules and OpenAPI configuration for `relationship-service` and `media-service`.

## [2026-10-01] — SCRUM-35

### Changed
- **apiGateway**: Added deprecation rewrite rules for legacy `/api/*` endpoints to rewrite to `/api/v1/*` with `Deprecation: true` headers.
- **userService**: Migrated all endpoints to `/api/v1/` prefix. Updated `LeaderboardController` to return `CursorPageResponse`.
- **activityService**: Migrated all endpoints to `/api/v1/` prefix.
- **aiService**: Migrated all endpoints to `/api/v1/` prefix. Refactored `RecommendationController` to return `RecommendationResponseDto` and `CursorPageResponse`.
- **configServer**: Updated `api-gateway.yml` to support OpenAPI routes and `/api/v1/` routing.
- **pom.xml**: Added `springdoc-openapi-starter` to `userService`, `activityService`, `aiService`, and `apiGateway` for Swagger UI generation.

### Added
- **userService**: Added `CursorPageResponse` DTO wrapper for pagination.
- **aiService**: Added `CursorPageResponse` and `RecommendationResponseDto`.

### Documentation Updated
- `api-curls.md` — Updated all endpoints to `/api/v1/`.
- `project/services/ai_service.md` — Updated endpoints and DTOs.
- `project/services/user_service.md` — Updated endpoints.
- `project/services/activity_service.md` — Updated endpoints.
- `project/services/api_gateway.md` — Documented deprecation rewrite filter and swagger UI.
- `project/Wefit_Project_Overview.md` — Added OpenAPI/Swagger documentation reference.


### Added
- **DevOps/Performance**: Added Redis Caching.
- **DevOps/Performance**: Injected `spring-boot-starter-data-redis` and `spring-boot-starter-cache` into `userService` and `activityService`.
- **DevOps/Performance**: Added `@EnableCaching` to main application classes and `@Cacheable` to heavy-read endpoints (e.g. `getUserById`, `getUserProfile`, `getGlobalLeaderboard`).
- **DevOps/Performance**: Configured Redis in `docker-compose.yml` and added connection properties to `user-service.yml` in configServer.

## [2026-10-01] — SCRUM-59

### Added
- **DevOps**: Implemented Centralized Logging using Grafana Loki and `loki-logback-appender`.
- **DevOps**: Configured all Spring Boot microservices with a standard JSON `logback-spring.xml` which automatically pushes logs directly to the Loki API endpoint over HTTP.
- **DevOps**: Added Loki to `docker-compose.yml` and configured it as a datasource in Grafana for unified log aggregation and querying using LogQL.

## [2026-10-01] — SCRUM-58

### Added
- **DevOps**: Added Monitoring & Observability with Prometheus and Grafana.
- **DevOps**: Injected `micrometer-registry-prometheus` and `spring-boot-starter-actuator` into all Spring Boot microservices.
- **DevOps**: Configured global endpoint exposure in `configServer/application.yml` (`/actuator/prometheus`).
- **DevOps**: Created `prometheus.yml` scrape configuration to poll metrics from all microservices.
- **DevOps**: Provisioned a base "Wefit Microservices" Grafana Dashboard to track HTTP request rates and errors.

## [2026-10-01] — SCRUM-57

### Added
- **DevOps**: Added Kubernetes deployment manifests (`k8s/wefit-stack.yaml`).
- **DevOps**: Configured ConfigMaps and Secrets for environment variables.
- **DevOps**: Configured Deployments with appropriate replica counts and ClusterIP Services for all infrastructure (Postgres, MongoDB, Kafka, Keycloak) and Spring Boot microservices.
- **DevOps**: Configured Nginx Ingress Controller to route `/api/*` to `api-gateway` and `/*` to `wefit-web`.

## [2026-10-01] — SCRUM-56

### Added
- **DevOps**: Configured GitHub Actions CI/CD Pipeline (`ci.yml`).
- **DevOps**: Matrix build for all Java microservices (`mvn clean package`).
- **DevOps**: Build step for Node.js frontend (`wefit-web`).
- **DevOps**: Added Docker image build and push steps to publish images to Docker Hub on `main` branch merges.

## [2026-10-01] — SCRUM-55

### Added
- **DevOps**: Added Docker containerization for the full Wefit stack.
- **DevOps**: Created optimized multi-stage `Dockerfile`s for all Spring Boot microservices (`userService`, `activityService`, `aiService`, `eureka`, `configServer`, `apiGateway`) and Node.js `wefit-web`.
- **DevOps**: Created `docker-compose.yml` for unified orchestration including PostgreSQL, MongoDB, Kafka, ZooKeeper, and Keycloak with proper volume mounts and health checks.
- **DevOps**: Added `docker-compose.dev.yml` and `docker-compose.prod.yml` to support local debugging and production-like deployment profiles.
- **Documentation**: Updated `README.md` with instructions on how to start the stack using Docker Compose.

## [2026-10-01] — SCRUM-48

### Added
- **userService**: Added Gamification Leaderboards.
- **userService**: Added `LeaderboardService` to query users by XP.
- **userService**: Added `LeaderboardController` with `/api/v1/leaderboard/global` endpoint.

## [2026-10-01] — SCRUM-49

### Added
- **userService**: Added Gamification Badges and Achievements system.
- **userService**: Added `Badge` and `UserBadge` entities.
- **userService**: Added `BadgeRepository` and `UserBadgeRepository`.
- **userService**: Added `BadgeService` to evaluate and unlock milestone badges (e.g. 7-Day Streak, Level 10).
- **userService**: Integrated badge evaluation into `GamificationService`'s XP and streak granting workflow.

## [2026-10-01] — SCRUM-50

### Added
- **userService**: Added Gamification Streak tracking.
- **userService**: Updated `User` entity to include `currentStreak`, `longestStreak`, and `streakFreezes`.
- **userService**: Updated `GamificationService` to manage streak increments, streak freezes, and missed days logic.
- **userService**: Updated `UserResponseDto` to expose streak-related fields for profile display.

## [2026-10-01] — SCRUM-51

### Added
- **userService**: Added Gamification XP and Levels system.
- **userService**: Added `GamificationService` to manage XP logic, daily limits, and level calculation.
- **userService**: Added `XpHistory` entity and `XpHistoryRepository` for tracking XP breakdown.
- **userService**: Added `ActivityKafkaListener` to consume `activity-events` and grant XP.
- **userService**: Updated `UserResponseDto` to expose `xp` and `level` fields.
- **configServer**: Added Kafka consumer configuration for `user-service`.

## [2026-10-01] — SCRUM-34

### Added
- **Global**: Created `.github/workflows/ci.yml` for running Maven tests on pull requests.
- **userService**: Added `GlobalExceptionHandlerTest` for unit testing exception handler.
- **userService**: Added `PostgresIntegrationTest` using Testcontainers for PostgreSQL integration testing.
- **activityService**: Added `KafkaIntegrationTest` using Testcontainers for Kafka integration testing.
- **Dependencies**: Added `spring-boot-starter-webmvc-test`, `spring-boot-starter-test`, `testcontainers`, and `jacoco-maven-plugin` to `pom.xml` across all services.

## [2026-09-27] — SCRUM-52, SCRUM-37, SCRUM-39

### Added
- **wefit-web**: Created new React frontend application using Vite, Keycloak, and vanilla CSS.
- **wefit-web**: Implemented luxury design system with dark OLED background and gold accents.
- **wefit-web**: Created base UI screens for Landing Page, Dashboard, Activity Log, and AI Insights.
- **wefit-web**: Added placeholders for Feed (SCRUM-37) and Notifications (SCRUM-39).
- **Stitch**: Generated high-fidelity luxury UI components using Stitch MCP.

### Documentation Updated
- `project/services/wefit-web.md` — Created documentation for the new frontend web app.
- `project/Wefit_Project_Overview.md` — Added wefit-web to Roadmap and Architecture.
- `README.md` — Marked frontend client as done.

### Added
- **API Gateway**: Implemented global CORS configuration (`globalcors`) in `api-gateway.yml` to allow frontend clients to access the APIs.
- **API Gateway**: Added `.cors(Customizer.withDefaults())` to `SecurityConfiguration.java` to handle CORS for Spring WebFlux security layer.

### Documentation Updated
- `project/services/api_gateway.md` — Noted CORS configuration in SecurityConfiguration and configuration sections.
- `project/Wefit_Project_Overview.md` — Added `CORS_ALLOWED_ORIGINS` to Environment Variables.
- `.agents/rules/security.md` — Marked CORS configuration vulnerability as Fixed.

---

## [2026-09-20] — SCRUM-32

### Changed
- **UserService**: Disabled `ddl-auto: update` and introduced Flyway for database migrations. Added `spring.flyway.enabled: true` and `baseline-on-migrate: true` to configuration.

### Added
- **UserService**: Added `flyway-core` and `flyway-database-postgresql` dependencies. Created initial migration script `V1__create_users_table.sql` with indexes for `email`, `user_name`, and `keycloak_id`.

### Documentation Updated
- `project/services/user_service.md` — Updated ORM details to reflect Flyway usage and fixed typo in column name.
- `project/Wefit_Project_Overview.md` — Noted Flyway in Tech Stack Summary.
- `.agents/rules/database.md` — Updated DDL Strategy for PostgreSQL.


## [2026-09-19] — SCRUM-31

### Fixed
- **UserService**: Fixed `updadatedDateTime` typo across `User` entity and DTO. Removed duplicated mapping logic. Standardized Maven `groupId` to `com.wefit`.
- **ActivityService**: Migrated `ActivityRepository` ID type to `String` (for MongoDB ObjectId). Cleaned up package naming (`repository`) and factory method naming (`fromDto`).
- **AiService**: Corrected missing slash in `RecommendationController` base path (`/api/recommendations`).

---

## [2026-09-19] — SCRUM-30

### Added
- **Backend Services**: Implemented `@ControllerAdvice` via `GlobalExceptionHandler` across `UserService`, `ActivityService`, and `AiService`.
- **Backend Services**: Error responses now natively format as RFC 7807 `ProblemDetail`. Stack traces are hidden and a UUID correlation ID is added to help with debugging.
- **Backend Services**: Custom exceptions added: `UserNotFoundException`, `UserConflictException`, `ActivityNotFoundException`, `InvalidActivityException`, `AiProcessingException`.

---

## [2026-09-19] — SCRUM-29

### Added
- **API Gateway**: Implemented `XssSanitizationFilter` as a global filter to sanitize POST/PUT/PATCH JSON bodies using `owasp-java-html-sanitizer`.
- **API Gateway**: Added `RequestSize` default filter limiting requests to 5MB.

---

## [2026-09-19] — SCRUM-28

### Added
- **API Gateway**: Added Redis-backed rate limiting using `RequestRateLimiter` default filter (100 req/s, 200 burst).
- **API Gateway**: Created `RateLimiterConfig` with `KeyResolver` to rate limit by user ID or IP address.
- **API Gateway**: Added `spring-boot-starter-data-redis-reactive` dependency.

### Documentation Updated
- `project/services/api_gateway.md` — Added RateLimiterConfig and rate limit filter details.
- `project/Wefit_Project_Overview.md` — Added Redis env vars.
- `.env.example` — Added `REDIS_HOST` and `REDIS_PORT`.

---

## [0.0.1-SNAPSHOT] — 2026-06-11

### Commit: `8add452` — feat: integrate Eureka server and implement inter-service communication via WebClient

**Added:**
- Eureka Server service for service discovery (port 8761)
- ActivityService with MongoDB integration and Kafka producer configuration
- AiService with MongoDB integration and Kafka consumer
- WebClient-based inter-service communication (ActivityService → UserService)
- `@LoadBalanced` WebClient for Eureka-based service resolution
- `UserValidationService` for cross-service user validation
- `ActivityMessageListener` Kafka consumer in AiService
- `RecommendationService` with placeholder recommendation generation
- `RecommendationController` with REST endpoints for fetching recommendations
- `MongoConfigurations` for enabling MongoDB auditing in ActivityService
- Startup scripts: `start-services.bat` (Windows) and `start_services.py` (Python)

### Commit: `33b3282` — First stage completed added register and get user profile endpoints

**Added:**
- UserService with PostgreSQL integration
- User entity with JPA mappings
- `AuthController` with registration and validation endpoints
- `UserController` with profile lookup endpoints
- DTOs: `UserRequestDto` (with Jakarta validation), `UserResponseDto`
- `UserRepository` with custom query methods

### Commit: `3057e60` — Initial commit

**Added:**
- Initial project structure
- Maven wrapper for all services

---

## Maintenance Notes

This changelog should be updated with every code change. Include:
- Date and commit hash
- Category: Added / Changed / Fixed / Removed / Security / Deprecated
- Brief description of what changed and why
