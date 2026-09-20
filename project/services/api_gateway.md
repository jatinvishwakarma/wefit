# 🌐 API Gateway — Reverse Proxy, Routing & Authentication

> **Module:** `apiGateway/`
> **Spring Name:** `api-gateway`
> **Port:** `8443` (HTTPS) / `8085` (HTTP → HTTPS redirect)
> **Database:** None
> **Last Updated:** 2026-09-19

---

## Purpose

The API Gateway is the **single entry point** for all client-facing traffic. It performs four critical jobs:

1. **Routing:** Forwards incoming requests to the correct microservice based on URL path patterns.
2. **Authentication:** Validates JWT Bearer tokens against Keycloak's JWK endpoint.
3. **User Synchronisation:** Automatically syncs Keycloak-authenticated users into the User Service database.
4. **TLS Termination & HTTP Redirect:** Serves HTTPS on port 8443 and redirects plain HTTP (port 8085) to HTTPS.

---

## Directory Structure

```
apiGateway/
├── src/main/java/com/wefit/apiGateway/
│   ├── ApiGatewayApplication.java       ← Main Spring Boot class
│   ├── SecurityConfiguration.java       ← WebFlux security (OAuth2 Resource Server)
│   ├── config/
│   │   └── HttpToHttpsRedirectConfig.java  ← HTTP→HTTPS redirect server
│   ├── filter/
│   │   └── KeyCloakUserSyncFilter.java  ← JWT parsing + auto-register WebFilter
│   └── user/
│       ├── UserRequestDto.java          ← DTO for user auto-registration
│       ├── UserRole.java                ← Enum: USER, ADMIN, COACH
│       └── WebClientConfig.java         ← WebClient bean for User Service calls
├── src/main/resources/
│   └── application.yml                  ← Local config (bootstrap to Config Server)
└── pom.xml
```

---

## Class-by-Class Deep Dive

### 1. ApiGatewayApplication.java

```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

Standard Spring Boot entry point. Note: **no** `@EnableEurekaClient` annotation is needed — Spring Cloud auto-detects Eureka on the classpath.

---

### 2. SecurityConfiguration.java

**Path:** `apiGateway/src/main/java/com/wefit/apiGateway/SecurityConfiguration.java`

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
```

**Key Details:**
- **`@EnableWebFluxSecurity`**: The Gateway uses Spring WebFlux (reactive), NOT Spring MVC. This is because Spring Cloud Gateway is built on top of Project Reactor and Netty.
- **CSRF disabled**: APIs are stateless (JWT-based), so CSRF protection is unnecessary.
- **Actuator public**: Health checks and monitoring endpoints are accessible without auth.
- **Everything else authenticated**: Requires a valid JWT Bearer token.
- **`oauth2ResourceServer.jwt()`**: Validates JWTs using the JWK Set URI from Keycloak.
- **CORS enabled**: Uses `.cors(Customizer.withDefaults())` to allow WebFlux security to process preflight requests in conjunction with Spring Cloud Gateway's global CORS configuration.

---

### 3. RateLimiterConfig.java

**Path:** `apiGateway/src/main/java/com/wefit/apiGateway/config/RateLimiterConfig.java`

```java
@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .defaultIfEmpty(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}
```

**Key Details:**
- Provides the `KeyResolver` bean (`userKeyResolver`) used by Spring Cloud Gateway's `RequestRateLimiter`.
- Resolves the rate-limiting key by extracting the authenticated user's ID (`Principal.getName()`).
- Falls back to the remote IP address if the principal is unavailable.

---

### 4. HttpToHttpsRedirectConfig.java

**Path:** `apiGateway/src/main/java/com/wefit/apiGateway/config/HttpToHttpsRedirectConfig.java`

This class starts a **separate HTTP server** on port 8085 that redirects all incoming requests to HTTPS on port 8443.

**How it works:**
- Uses raw **Reactor Netty** (`HttpServer.create()`) — NOT Spring Boot's embedded server.
- On `@PostConstruct`, it binds to `httpPort` (8085) and handles every request by:
  1. Extracting the `Host` header (stripping the port if present).
  2. Building the HTTPS URL with the HTTPS port.
  3. Responding with `301 Moved Permanently` and a `Location` header.
- On `@PreDestroy`, it gracefully shuts down the redirect server.

**Technical Note:** The `status()` method uses `HttpStatus.MOVED_PERMANENTLY.value()` (integer `301`) because Reactor Netty's `HttpServerResponse` expects an integer or Netty's `HttpResponseStatus`, NOT Spring's `HttpStatus` enum.

---

### 5. KeyCloakUserSyncFilter.java ⭐

**Path:** `apiGateway/src/main/java/com/wefit/apiGateway/filter/KeyCloakUserSyncFilter.java`

This is the **most important custom component** in the Gateway. It runs as a `WebFilter` with `@Order(1)` (highest priority), executing before Spring Cloud Gateway routes the request.

**Flow:**

```
Incoming Request
  ↓
1. Extract "Authorization: Bearer <token>" header
  ↓ (no token? → skip, let security handle rejection)
2. Parse JWT using nimbus-jose-jwt (SignedJWT.parse)
  ↓
3. Extract claims: sub, email, given_name, family_name, preferred_username
  ↓
4. GET /api/users/keycloak/{keycloakId} → User Service
  ↓
5a. User FOUND → extract internal ID
5b. User NOT FOUND (404) → POST /api/users/register (auto-register)
     Password set to "KEYCLOAK_MANAGED" (Keycloak handles auth)
  ↓
6. Add headers to downstream request:
   - X-User-Id: {internalLongId}
   - X-User-Keycloak-Id: {keycloakSubject}
  ↓
7. Continue filter chain → Spring Cloud Gateway routes the request
```

**Why this exists:** Keycloak manages authentication, but our downstream services (Activity, AI) need the internal database user ID. This filter bridges the gap — it ensures every Keycloak user has a corresponding record in our User Service database, and injects the internal ID as a header.

### Filters & Security

| Filter | Purpose | Details |
| :--- | :--- | :--- |
| **`KeyCloakUserSyncFilter`** | Auto-registration | Intercepts HTTP 404 from `UserService`, extracts user details from JWT, and automatically registers the user via `POST /api/user/sync`. |
| **`RequestRateLimiter`** | Rate Limiting | Default filter using Redis. Configured for 100 req/s replenish rate and 200 burst capacity. Uses `userKeyResolver` for per-user/IP limiting. |
| **`RequestSize`** | DoS Protection | Default filter rejecting requests with bodies larger than 5MB (HTTP 413). |
| **`XssSanitizationFilter`** | Input Sanitization | Intercepts POST/PUT/PATCH JSON bodies, strips HTML/XSS scripts using OWASP HTML Sanitizer policy, and rewrites the body payload safely. |

**Internal class: `UserSyncResponse`** — A minimal DTO with `Long id` and `String keycloakId`, used only within this filter to deserialise User Service responses.

---

### 6. user/UserRequestDto.java

**Path:** `apiGateway/src/main/java/com/wefit/apiGateway/user/UserRequestDto.java`

A gateway-local DTO used by `KeyCloakUserSyncFilter` to register new users. Fields:

| Field          | Type       | Validation                             |
|----------------|-----------|----------------------------------------|
| `firstName`    | `String`  | `@NotBlank`                            |
| `lastName`     | `String`  | `@NotBlank`                            |
| `userName`     | `String`  | `@NotBlank`                            |
| `email`        | `String`  | `@NotBlank`, `@Email`                  |
| `password`     | `String`  | `@NotBlank` (set to `"KEYCLOAK_MANAGED"`) |
| `keycloakId`   | `String`  | —                                      |
| `phoneNumber`  | `String`  | —                                      |
| `bio`          | `String`  | —                                      |
| `gender`       | `String`  | —                                      |
| `dateOfBirth`  | `String`  | —                                      |
| `role`         | `UserRole`| —                                      |

---

### 6. user/UserRole.java

```java
public enum UserRole { USER, ADMIN, COACH }
```

Duplicated from User Service since the Gateway doesn't share a common library with downstream services.

---

### 7. user/WebClientConfig.java

Creates a `WebClient` bean pointed at the User Service base URL:

```java
@Bean
public WebClient userServiceWebClient() {
    return webClientBuilder().baseUrl(userServiceBaseUrl).build();
}
```

`userServiceBaseUrl` defaults to `http://localhost:8081` but is overridden by the Config Server to `https://localhost:8081`.

---

## Routing Configuration

The Gateway's routes are defined in `configServer/src/main/resources/config/api-gateway.yml`:

| Route ID             | Path Predicate            | Target Service             |
|----------------------|---------------------------|----------------------------|
| `user-service`       | `/api/user/**`            | `lb://user-service`        |
| `user-service-2`     | `/api/users/**`           | `lb://user-service`        |
| `activity-service`   | `/api/activity/**`        | `lb://activity-service`    |
| `activity-service-2` | `/api/activities/**`      | `lb://activity-service`    |
| `ai-service`         | `/api/ai/**`              | `lb://ai-service`          |
| `ai-service-2`       | `/api/recommendations/**` | `lb://ai-service`          |

- `lb://` prefix means load-balanced via Eureka. The Gateway queries Eureka for instances of (e.g.) `user-service` and forwards the request to one of them.
- Each service has two routes to support different URL patterns.

---

## Configuration

### Local: `application.yml`
```yaml
spring:
  application:
    name: api-gateway
  config:
    import: optional:configserver:http://localhost:8888
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${KEYCLOAK_CERTS_URL:http://localhost:8090/realms/wefit/protocol/openid-connect/certs}

server:
  port: 8085

user-service:
  base-url: ${USER_SERVICE_URL:http://localhost:8081}
```

Note: The local `application.yml` sets port `8085` as a fallback, but Config Server overrides it to `8443` with SSL.

### From Config Server: `api-gateway.yml`
Sets the actual HTTPS port (8443), all 6 gateway routes, Eureka registration, Keycloak JWK URI, global CORS filtering, debug logging, and User Service URL.

---

## Design Decisions

1. **WebFlux (Reactive):** Spring Cloud Gateway requires a reactive stack (WebFlux + Netty), not the traditional Servlet stack. This is why all filters and security configs use reactive types (`Mono`, `ServerHttpSecurity`, etc.).
2. **Separate HTTP redirect server:** Since the main server runs on HTTPS, we use a raw Reactor Netty server for the HTTP→HTTPS redirect. This avoids needing a separate reverse proxy (like Nginx) for development.
3. **JWT parsing with Nimbus:** The `KeyCloakUserSyncFilter` parses JWTs directly using `nimbus-jose-jwt` instead of relying on Spring Security's authentication context, because the filter runs before Spring Security processes the token.
4. **Duplicate DTOs:** The Gateway has its own `UserRequestDto` and `UserRole` instead of sharing with User Service. In a future iteration, a shared library or API contracts module could eliminate this duplication.
