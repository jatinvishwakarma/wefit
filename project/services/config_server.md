# ⚙️ Config Server — Centralised Configuration

> **Module:** `configServer/`
> **Spring Name:** `config-server`
> **Port:** `8888`
> **Database:** None (filesystem-backed)
> **Last Updated:** 2026-09-19

---

## Purpose

The Config Server is a **Spring Cloud Config Server** that acts as the single source of truth for all configuration properties across every microservice. Instead of each service having its own database passwords, Kafka URLs, and Eureka endpoints hardcoded, they all ask the Config Server for their configuration on startup.

---

## Why Centralised Config?

| Without Config Server                        | With Config Server                             |
|---------------------------------------------|------------------------------------------------|
| Every service has its own secrets in YAML    | Secrets live in one place                       |
| Changing a password = editing 5+ files       | Change it once, restart the service            |
| Easy to forget to update one service         | Impossible to have inconsistent config          |
| Secrets scattered across git commits         | Can be externalised (Vault, encrypted, etc.)   |

---

## Directory Structure

```
configServer/
├── src/main/java/com/wefit/configServer/
│   └── ConfigServerApplication.java       ← Main class with @EnableConfigServer
├── src/main/resources/
│   ├── application.yml                     ← Server's own config (port, SSL, Eureka)
│   └── config/                             ← THE CONFIG FILES served to other services
│       ├── user-service.yml
│       ├── activity-service.yml
│       ├── ai-service.yml
│       └── api-gateway.yml
└── pom.xml
```

---

## Key File: ConfigServerApplication.java

**Path:** `configServer/src/main/java/com/wefit/configServer/ConfigServerApplication.java`

```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

- `@EnableConfigServer` activates the embedded Spring Cloud Config Server.
- The server exposes a REST API (e.g., `GET /{application}/{profile}`) that other services call.

---

## Server Configuration

**File:** `configServer/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: config-server
  profiles:
    active: native                       # ← Use local filesystem, NOT git
  cloud:
    config:
      server:
        native:
          search-location: classpath:/config  # ← Look in resources/config/

server:
  port: 8888
  ssl:
    key-store: file:../certs/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: wefit

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_DEFAULT_ZONE:https://localhost:8761/eureka}
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    secure-port-enabled: true
    non-secure-port-enabled: false
```

### Key Points

| Property                              | Value                    | Why                                                    |
|---------------------------------------|-------------------------|--------------------------------------------------------|
| `spring.profiles.active`             | `native`                | Reads configs from local filesystem (not a Git repo)  |
| `cloud.config.server.native.search-location` | `classpath:/config` | Configs are packaged inside the JAR under `/config/`   |
| `server.port`                         | `8888`                  | Standard Spring Cloud Config port                      |
| SSL                                    | Enabled (PKCS12)        | All inter-service communication is over HTTPS          |

---

## Served Configuration Files

### `config/user-service.yml`
Provides to User Service:
- PostgreSQL datasource URL, username, password
- JPA/Hibernate settings (`ddl-auto: update`, PostgreSQL dialect, `show-sql: true`)
- Server port `8081` with SSL
- Eureka registration settings

### `config/activity-service.yml`
Provides to Activity Service:
- MongoDB URI and database name
- Kafka bootstrap servers and producer serialiser settings
- Kafka topic name (`activity-events`)
- User Service base URL for validation (`https://localhost:8081`)
- Server port `8082` with SSL
- Eureka registration settings

### `config/ai-service.yml`
Provides to AI Service:
- MongoDB URI and database name
- Kafka bootstrap servers and consumer settings (group ID, deserialisers, trusted packages)
- Kafka topic name (`activity-events`)
- Gemini API URL and API key
- Server port `8083` with SSL
- Eureka registration settings

### `config/api-gateway.yml`
Provides to API Gateway:
- Keycloak JWK Set URI
- All Spring Cloud Gateway route definitions (6 routes covering user/activity/ai services)
- Gateway discovery locator settings
- Server port `8443` with SSL
- Eureka registration settings
- Debug logging levels for gateway/loadbalancer
- User Service base URL for the KeyCloakUserSyncFilter

---

## How Clients Consume Config

Each client service has this in its own `application.yml`:

```yaml
spring:
  application:
    name: user-service                   # ← This name maps to config/user-service.yml
  config:
    import: optional:configserver:http://localhost:8888
```

The `optional:` prefix means if the Config Server is unreachable, the service still starts (with defaults). The `spring.application.name` is the lookup key — it matches the filename in the `config/` directory.

---

## Design Decisions

1. **Native (filesystem) over Git:** For local development, using the filesystem is simpler. For production, this could be switched to a Git backend for versioning and audit trails.
2. **`optional:` import prefix:** Prevents cascading startup failures if Config Server is temporarily down.
3. **Environment variable placeholders:** Configs use `${ENV_VAR:default}` syntax so they work in both local dev (from `.env`) and production (from container env).
