# WeFit — Dependencies

## Shared Across All Services

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Spring Boot Parent | 4.0.6 | Parent POM, version management |
| Spring Cloud Dependencies | 2025.1.1 | Cloud BOM for Eureka, etc. |
| Java | 21 | Language runtime |
| spring-boot-maven-plugin | (managed) | Packaging executable JARs |
| maven-compiler-plugin | (managed) | Compilation with Lombok annotation processing |

## Eureka Server

| Dependency | Scope | Purpose |
|-----------|-------|---------|
| spring-cloud-starter-netflix-eureka-server | compile | Eureka server implementation |
| spring-boot-starter-test | test | Test framework |

## UserService

| Dependency | Scope | Purpose |
|-----------|-------|---------|
| spring-boot-starter-webmvc | compile | REST API (Spring MVC) |
| spring-boot-starter-data-jpa | compile | JPA / Hibernate ORM |
| spring-boot-starter-validation | compile | Jakarta Bean Validation |
| spring-boot-starter-webflux | compile | WebClient for inter-service calls |
| spring-cloud-starter-netflix-eureka-client | compile | Eureka service registration |
| postgresql | runtime | PostgreSQL JDBC driver |
| lombok | compile (optional) | Boilerplate reduction |
| spring-boot-devtools | runtime (optional) | Hot reload during development |
| spring-boot-starter-validation-test | test | Validation test support |
| spring-boot-starter-webmvc-test | test | MockMvc test support |

> **Note**: `spring-boot-starter-webflux` is included alongside `spring-boot-starter-webmvc` solely for `WebClient`. The app runs as a servlet (MVC) application, not reactive.

## ActivityService

| Dependency | Scope | Purpose |
|-----------|-------|---------|
| spring-boot-starter-webmvc | compile | REST API (Spring MVC) |
| spring-boot-starter-data-mongodb | compile | MongoDB access |
| spring-boot-starter-webflux | compile | WebClient for UserService calls |
| spring-kafka | compile | Kafka producer |
| spring-cloud-starter-netflix-eureka-client | compile | Eureka service registration |
| lombok | compile (optional) | Boilerplate reduction |
| spring-boot-devtools | runtime (optional) | Hot reload |
| spring-boot-starter-webmvc-test | test | MockMvc test support |

## AiService

| Dependency | Scope | Purpose |
|-----------|-------|---------|
| spring-boot-starter-webmvc | compile | REST API (Spring MVC) |
| spring-boot-starter-data-mongodb | compile | MongoDB access |
| spring-kafka | compile | Kafka consumer |
| spring-cloud-starter-netflix-eureka-client | compile | Eureka service registration |
| lombok | compile (optional) | Boilerplate reduction |
| spring-boot-devtools | runtime (optional) | Hot reload |
| spring-boot-starter-test | test | Test framework |

## Lombok Annotation Processing

All services (except Eureka) configure the `maven-compiler-plugin` to explicitly register Lombok as an annotation processor for both `compile` and `testCompile` phases:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
</annotationProcessorPaths>
```

## GroupId Inconsistency

| Service | GroupId |
|---------|---------|
| eureka | `com.wefit` |
| activityService | `com.wefit` |
| aiService | `com.wefit` |
| userService | `com.example` ⚠️ |

> **Note**: `userService` uses `com.example` instead of `com.wefit`. This should be corrected for consistency.

## External Infrastructure Dependencies

| Infrastructure | Default Location | Required By |
|---------------|-----------------|-------------|
| PostgreSQL | `localhost:5432` | UserService |
| MongoDB | `localhost:27017` | ActivityService, AiService |
| Apache Kafka | `localhost:9092` | ActivityService, AiService |
| Eureka Server | `localhost:8761` | All client services |
