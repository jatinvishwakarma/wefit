# 🔍 Eureka Server — Service Discovery

> **Module:** `eureka/`
> **Spring Name:** `eureka`
> **Port:** `8761`
> **Database:** None
> **Last Updated:** 2026-09-19

---

## Purpose

Eureka Server is the **service discovery registry** for the entire Wefit platform. When any microservice starts up, it registers itself with Eureka, advertising its host, port, and health status. Other services (like the API Gateway) query Eureka to discover available instances at runtime, enabling:

- **Dynamic routing:** No hardcoded IP/port pairs between services.
- **Load balancing:** The `lb://` URI scheme in Spring Cloud Gateway resolves through Eureka and can distribute traffic across multiple instances of the same service.
- **Health monitoring:** Eureka periodically checks registered service heartbeats and evicts unreachable instances.

---

## Why Eureka?

In a microservices architecture, services spin up and down dynamically. Hardcoding service addresses breaks the moment you scale to multiple instances or deploy to different environments. Eureka solves this by acting as a central phone book — every service says "I'm here" and others look up where to find each other.

---

## Directory Structure

```
eureka/
├── src/main/java/com/wefit/eureka/
│   └── EurekaApplication.java          ← Main class with @EnableEurekaServer
├── src/main/resources/
│   └── application.yml                  ← Local config (port, SSL, self-registration disabled)
└── pom.xml
```

---

## Key File: EurekaApplication.java

**Path:** `eureka/src/main/java/com/wefit/eureka/EurekaApplication.java`

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
```

**What this does:**
- `@EnableEurekaServer` activates the Netflix Eureka Server embedded in this Spring Boot application. Without this annotation, it would be a regular Spring Boot app that does nothing.
- This is the **only** Java class in the Eureka module — the rest is configuration.

---

## Configuration

**File:** `eureka/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: eureka
server:
  port: 8761
  ssl:
    key-store: file:../certs/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: wefit

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### Key Configuration Explained

| Property                        | Value      | Why                                                                 |
|---------------------------------|-----------|----------------------------------------------------------------------|
| `server.port`                   | `8761`    | Standard Eureka port. All other services reference this.             |
| `server.ssl.*`                  | PKCS12    | Eureka runs over HTTPS for secure inter-service communication.       |
| `eureka.client.register-with-eureka` | `false`  | Eureka doesn't register with itself (it IS the registry).           |
| `eureka.client.fetch-registry`  | `false`   | Eureka doesn't need to fetch its own registry.                       |

---

## How Other Services Register

Every other service (User, Activity, AI, Config Server, Gateway) has this in their config:

```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_DEFAULT_ZONE:https://localhost:8761/eureka}
  instance:
    prefer-ip-address: true
    secure-port-enabled: true
    non-secure-port-enabled: false
```

This means:
1. On startup, they POST to `https://localhost:8761/eureka/apps/{serviceName}`.
2. They send heartbeats every 30 seconds (default).
3. Eureka evicts them after 90 seconds of no heartbeat (default).

---

## Dashboard

Once running, you can view the Eureka dashboard at:
```
https://localhost:8761
```

This shows all registered services, their instances, status (UP/DOWN), and metadata.

---

## Design Decision: Why Not Kubernetes Service Discovery?

We chose Eureka over Kubernetes-native service discovery because:
1. The platform runs locally during development without Kubernetes.
2. Eureka integrates natively with Spring Cloud and requires zero additional infrastructure.
3. For future production, we can swap to Kubernetes service discovery with minimal changes.
