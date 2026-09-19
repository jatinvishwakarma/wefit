# WeFit — Changelog

All notable changes to this project are documented in this file.

---

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
