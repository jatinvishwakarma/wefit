---
name: project-orientation
description: >-
  MANDATORY first step before starting ANY work on the Wefit project. Read this
  skill to get a complete map of what exists where — rules, skills, documentation,
  key source files, coding patterns, and working constraints — so you never start
  blind. Activate this whenever you begin a new session, pick up a Jira ticket,
  or are about to make any code change.
---

# Wefit Project Orientation

> **You MUST read this before touching any code, creating any file, or starting any task.**
> This is your project map. It tells you what exists, where it lives, and what rules govern this codebase.

---

## 1. What is Wefit? (30-second summary)

Wefit is a **Spring Cloud Microservices** fitness platform. Users log workouts, an event is published to **Kafka**, the **AI Service** consumes it and calls **Google Gemini** to generate personalised recommendations.

**6 services, each its own Spring Boot app:**

| Service           | Port       | DB                              | Key Role                        |
|-------------------|------------|---------------------------------|---------------------------------|
| `eureka`          | 8761       | None                            | Service discovery               |
| `configServer`    | 8888       | None (filesystem)               | Centralised config              |
| `apiGateway`      | 8443/8085  | None                            | Routing, auth, user-sync        |
| `userService`     | 8081       | PostgreSQL (`wefit`)            | User registration & profiles    |
| `activityService` | 8082       | MongoDB (`WefitActivitydb`)     | Workout logging, Kafka producer |
| `aiService`       | 8083       | MongoDB (`AiRecommendationsdb`) | Kafka consumer, Gemini AI       |

**Full architecture & data flow:** Read `c:\Wefit\project\Wefit_Project_Overview.md`

---

## 2. Map of ALL Rules (`.agents/rules/`)

Rules are **always-active** constraints. They load automatically. Know what each one covers so you don't contradict them.

| File                          | What it governs                                                      |
|-------------------------------|----------------------------------------------------------------------|
| `project-overview.md`         | Tech stack, service ports, repo structure, known tech debt           |
| `architecture.md`             | Mermaid diagram, sync/async communication, startup order             |
| `wefit-complete-doc.md`       | **Exhaustive** method-by-method technical spec for every class       |
| `coding-standards.md`         | Package layout, Lombok patterns, DI style, DTO conventions           |
| `backend-development.md`      | Spring Boot development rules                                        |
| `api-design.md`               | REST endpoint design conventions                                     |
| `api-reference.md`            | Complete API endpoint reference (all routes, methods, payloads)      |
| `database.md`                 | General DB usage rules                                               |
| `database-schema.md`          | PostgreSQL and MongoDB schema details                                |
| `business-domain.md`          | Domain concepts, entities, business logic explanations               |
| `security.md`                 | Keycloak OAuth2, JWT validation, KeyCloakUserSyncFilter details      |
| `spring-boot.md`              | Spring Boot and Spring Cloud specific conventions                    |
| `dependencies.md`             | Approved libraries and version constraints                           |
| `testing.md`                  | Testing standards and patterns                                       |
| `debugging.md`                | Debugging strategies for common issues                               |
| `troubleshooting.md`          | Known issues and their solutions                                     |
| `code-review.md`              | Code review checklist                                                |
| `deployment.md`               | Deployment procedures (local + production)                           |
| `project-specific-patterns.md`| Patterns unique to this codebase (entity conversion, etc.)          |
| `workflows.md`                | End-to-end workflow sequence diagrams                                |
| `changelog.md`                | History of significant changes                                       |

> **Before adding anything new**, check `coding-standards.md`, `project-specific-patterns.md`, and `wefit-complete-doc.md` first to see if the pattern already exists.

---

## 3. Map of ALL Skills (`.agents/skills/`)

Skills are **on-demand** instructions. You invoke them for specific tasks.

| Skill                | When to use it                                                          |
|----------------------|-------------------------------------------------------------------------|
| `project-orientation`| This file. Read first, always, before any work session.                 |
| `jira-ticket-flow`   | Whenever starting a Jira ticket (branching, planning, coding, PR)       |
| `knowledge-sync`     | After EVERY code change — propagates updates to ALL rules, docs, skills |

---

## 4. Map of ALL Project Documentation (`project/`)

Detailed technical docs for every service:

| File                                    | Covers                                                                |
|-----------------------------------------|-----------------------------------------------------------------------|
| `project/Wefit_Project_Overview.md`     | Platform-wide: architecture, inter-service comms, env vars, startup  |
| `project/services/eureka.md`            | Eureka server: config, registration, dashboard                        |
| `project/services/config_server.md`     | Config Server: native profile, served YAMLs, client bootstrap        |
| `project/services/api_gateway.md`       | Gateway: routing table, SecurityConfig, KeyCloakUserSyncFilter        |
| `project/services/user_service.md`      | User Service: DB schema, all endpoints, service logic, queries        |
| `project/services/activity_service.md`  | Activity Service: MongoDB schema, Kafka producer, user validation     |
| `project/services/ai_service.md`        | AI Service: Kafka consumer, Gemini prompt, response parsing           |

> **Before modifying any service**, read its corresponding doc first.

---

## 5. Key Source Files to Know

The highest-impact files — know where they are before starting any session.

### API Gateway
- `apiGateway/src/main/java/com/wefit/apiGateway/SecurityConfiguration.java` — WebFlux security, OAuth2, JWT
- `apiGateway/src/main/java/com/wefit/apiGateway/filter/KeyCloakUserSyncFilter.java` — Auto-sync Keycloak users
- `apiGateway/src/main/java/com/wefit/apiGateway/config/HttpToHttpsRedirectConfig.java` — HTTP to HTTPS redirect
- `configServer/src/main/resources/config/api-gateway.yml` — All 6 gateway routes + Keycloak JWK URI

### User Service
- `userService/src/main/java/com/wefit/userService/service/UserService.java` — All business logic
- `userService/src/main/java/com/wefit/userService/entities/User.java` — JPA entity: users table schema
- `userService/src/main/java/com/wefit/userService/repository/UserRepository.java` — Custom queries
- `configServer/src/main/resources/config/user-service.yml` — PostgreSQL config + JPA settings

### Activity Service
- `activityService/src/main/java/com/wefit/activityService/service/ActivityService.java` — validate, save, publish to Kafka
- `activityService/src/main/java/com/wefit/activityService/entities/Activity.java` — MongoDB doc, String ID, metrics map
- `activityService/src/main/java/com/wefit/activityService/config/KafkaConfig.java` — Kafka producer config
- `configServer/src/main/resources/config/activity-service.yml` — MongoDB + Kafka + User Service URL

### AI Service
- `aiService/src/main/java/com/wefit/aiService/service/ActivityAiService.java` — Prompt engineering, response parsing
- `aiService/src/main/java/com/wefit/aiService/service/GeminiService.java` — RestClient to Gemini API
- `aiService/src/main/java/com/wefit/aiService/service/ActivityMessageListener.java` — Kafka consumer listener
- `aiService/src/main/java/com/wefit/aiService/config/KafkaConfig.java` — Consumer trusted packages, type headers off
- `configServer/src/main/resources/config/ai-service.yml` — Gemini API key + Kafka consumer settings

### Environment
- `.env.example` — Full reference of all env vars (DB, MongoDB, Kafka, Gemini, Keycloak, Eureka)

---

## 6. Non-Negotiable Coding Constraints

Violating any of these will be rejected in code review:

1. **No `@Autowired`** — All DI via constructor injection with Lombok (`@RequiredArgsConstructor` or `@AllArgsConstructor`).
2. **Never expose entity classes in controllers** — Always convert to a DTO first.
3. **Use `@Slf4j` for logging** — Not `System.out.println`.
4. **MongoDB entities use `String` ID** — NOT `Long`. ObjectId maps to String.
5. **Kafka producer: `spring.json.add.type.headers: false`** — Type headers must be disabled or the consumer breaks.
6. **Kafka consumer: `addTrustedPackages("*")` + `setUseTypeHeaders(false)`** — Different packages across services require this.
7. **Config Server is source of truth** — Never hardcode DB URLs, passwords, or API keys in service `application.yml`.
8. **Static factory methods for DTO conversion** — Follow the `toDto()` / `fromEntity()` pattern. No MapStruct.
9. **All services run over HTTPS** — SSL via PKCS12 keystore at `file:../certs/keystore.p12`.
10. **`ResourceNotFoundException` must return HTTP 404** — Requires `@ResponseStatus(HttpStatus.NOT_FOUND)`. The Gateway's `KeyCloakUserSyncFilter` catches 404s to trigger auto-registration.

---

## 7. Common Gotchas

| Symptom                                    | Root Cause and Fix                                                                |
|--------------------------------------------|-----------------------------------------------------------------------------------|
| `Invalid user` on activity log             | User Service down OR userId missing from PostgreSQL. Check `UserValidationService`.|
| Kafka consumer gets wrong class type       | `setUseTypeHeaders(false)` missing or `addTrustedPackages("*")` not set.          |
| AI recommendations not generating         | `GEMINI_API_KEY` env var missing. Check `ai-service.yml` and `.env`.              |
| Gateway returns 401 on all requests        | Keycloak not running on 8090, or `KEYCLOAK_CERTS_URL` not set.                   |
| `HttpServerResponse.status()` compile error| Use `HttpStatus.MOVED_PERMANENTLY.value()` (int), NOT the enum — Netty needs int. |
| User auto-registration not triggering      | `ResourceNotFoundException` missing `@ResponseStatus(NOT_FOUND)` — won't 404.    |
| `@CreatedDate` fields null in MongoDB      | `@EnableMongoAuditing` missing from `MongoConfigurations` class.                  |
| Services can't find each other             | Eureka not started first, or `EUREKA_DEFAULT_ZONE` env var not set.               |

---

## 8. Development Workflow Cheatsheet

```
Before starting ANY work:
  1. Read this file (done)
  2. Read the relevant service doc in project/services/
  3. Read the Jira ticket
  4. Use the jira-ticket-flow skill

When writing code:
  - Follow coding-standards.md
  - Check project-specific-patterns.md for existing patterns
  - Validate against api-design.md for new endpoints

Before committing:
  - Update project/services/{changed_service}.md
  - Update project/Wefit_Project_Overview.md if architectural change
  - Follow jira-ticket-flow steps 7 through 10
```

---

## 9. Environment Setup Checklist

Before running the project locally, ensure all of the following:

- [ ] Java 21 installed
- [ ] PostgreSQL running on 5432, database `wefit` created
- [ ] MongoDB running on 27017
- [ ] Apache Kafka running on 9092
- [ ] Keycloak running on 8090 with realm `wefit` configured
- [ ] TLS certs generated via `generate-certs.bat` (Windows) or `generate-certs.sh`
- [ ] `.env` copied from `.env.example` and filled with real values
- [ ] Services started via `start-services.bat` or `python start_services.py`
- [ ] All services visible at Eureka dashboard: `https://localhost:8761`
