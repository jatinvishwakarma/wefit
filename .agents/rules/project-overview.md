# WeFit — Project Overview

## What is WeFit?

WeFit is a **fitness tracking and AI-powered recommendation platform** built as a microservices architecture using Spring Boot. It allows users to register, log physical activities (running, cycling, yoga, etc.), and receive AI-generated recommendations based on their workout data.

## Technology Stack

| Layer              | Technology                                   |
|--------------------|----------------------------------------------|
| Language           | Java 21                                      |
| Framework          | Spring Boot 4.0.6                            |
| Cloud              | Spring Cloud 2025.1.1                        |
| Service Discovery  | Netflix Eureka                               |
| Messaging          | Apache Kafka                                 |
| Relational DB      | PostgreSQL (userService)                     |
| Document DB        | MongoDB (activityService, aiService)         |
| Build Tool         | Maven (wrapper: `mvnw` / `mvnw.cmd`)        |
| Boilerplate        | Lombok                                       |
| HTTP Client        | Spring WebFlux WebClient (LoadBalanced)      |
| Validation         | Jakarta Bean Validation (userService)        |

## Services Summary

| Service           | Port | Database                | Purpose                              |
|-------------------|------|-------------------------|--------------------------------------|
| Eureka            | 8761 | —                       | Service registry & discovery         |
| UserService       | 8081 | PostgreSQL `wefit`      | User registration & profile mgmt    |
| ActivityService   | 8082 | MongoDB `WefitActivitydb` | Fitness activity logging           |
| AiService         | 8083 | MongoDB `AiRecommendationsdb` | AI-based workout recommendations |

## Repository Structure

```
Wefit/
├── eureka/                    # Eureka server (service registry)
├── userService/               # User management microservice
├── activityService/           # Activity tracking microservice
├── aiService/                 # AI recommendation microservice
├── start-services.bat         # Windows batch startup script
├── start_services.py          # Python cross-platform startup script
└── .github/modernize/         # GitHub modernize hooks (Java upgrade)
```

Each service follows the standard Maven Spring Boot layout:
```
<service>/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/wefit/<service>/
    │   │   ├── <Service>Application.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── entities/
    │   │   ├── repositories/ (or repository/)
    │   │   └── service/
    │   └── resources/
    │       └── application.yml
    └── test/
```

## Current Project Status

- **Version**: 0.0.1-SNAPSHOT (all services)
- **Git History**: 3 commits (Initial → User endpoints → Eureka + inter-service communication)
- **Stage**: Early development / MVP
- **Tests**: Only Spring Boot default test stubs (no custom tests)
- **Security**: None implemented (no Spring Security, no JWT, plaintext passwords)

## Known Issues & Tech Debt

| Issue | Severity | Location |
|-------|----------|----------|
| Passwords stored in plaintext | 🔴 Critical | `UserService.registerUser()` |
| DB credentials hardcoded in YAML | 🔴 Critical | `userService/application.yml` |
| No authentication / authorization | 🔴 Critical | All services |
| No global exception handling | 🟡 Medium | All services |
| Typo: `updadatedDateTime` | 🟡 Medium | `User.java`, `UserResponseDto.java` |
| `ActivityRepository<Activity, Long>` but Activity ID is `String` | 🟡 Medium | `ActivityRepository.java` |
| Unused `@Component` import | 🟢 Low | `ActivityMessageListener.java` |
| Missing leading `/` in `@RequestMapping` | 🟢 Low | `RecommendationController.java` |
| Duplicate `toDto()` mapping logic | 🟢 Low | `User.java` + `UserResponseDto.java` |
| AI recommendations are hardcoded placeholder text | 🟡 Medium | `RecommendationService.java` |
