# Skill: Security

## Current Security State

> ⚠️ **CRITICAL**: WeFit has NO security implementation. This document describes the current state and recommended patterns.

### Known Security Vulnerabilities

| Issue | Severity | Location | Impact |
|-------|----------|----------|--------|
| Plaintext passwords | 🔴 Critical | `UserService.registerUser()` | Credential exposure if DB is breached |
| Hardcoded DB credentials | 🔴 Critical | `userService/application.yml` | Password `Jatin1307@` in source control |
| No authentication | 🔴 Critical | All controllers | Any client can access all endpoints |
| No authorization | 🔴 Critical | All controllers | No role-based access control |
| ~~No CORS configuration~~ | 🟢 Fixed | API Gateway | Global CORS configured via `api-gateway.yml` |
| No rate limiting | 🟡 Medium | All services | Vulnerable to brute force / DDoS |
| Trusted all Kafka packages | 🟡 Medium | `aiService/application.yml` | `spring.json.trusted.packages: "*"` |
| No HTTPS | 🟡 Medium | All services | Data transmitted in plaintext |
| No input sanitization | 🟡 Medium | All DTOs | Potential injection attacks |

## Recommended Security Implementation

### Phase 1: Password Hashing

```java
// Add to pom.xml
// spring-boot-starter-security (for BCryptPasswordEncoder)

// In UserService.registerUser()
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto registerUser(UserRequestDto dto) {
        // Hash password before saving
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                .password(hashedPassword)
                // ... other fields
                .build();
        return convertToResponseDto(userRepository.save(user));
    }
}
```

### Phase 2: JWT Authentication

```java
// Login endpoint pattern
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    User user = userRepository.findByUserNameOrEmail(request.getIdentifier(), request.getIdentifier())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BadCredentialsException("Invalid credentials");
    }

    String token = jwtService.generateToken(user);
    return ResponseEntity.ok(new AuthResponse(token));
}
```

### Phase 3: Externalize Secrets

```yaml
# application.yml — use env vars
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/wefit}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
```

### Phase 4: API Gateway

Add Spring Cloud Gateway as a front-door for all services:
- Centralized authentication
- Rate limiting
- CORS handling
- Request routing

## Internal Service-to-Service Security

Current internal calls (WebClient) have no authentication. For production:
- Use mutual TLS between services
- Or pass JWT tokens in service-to-service calls
- Or use a service mesh (Istio, Linkerd)

## Checklist for Security Review

- [ ] Are passwords hashed before storage?
- [ ] Are database credentials externalized?
- [ ] Are endpoints authenticated?
- [ ] Are role checks enforced?
- [ ] Is CORS configured properly?
- [ ] Are Kafka trusted packages scoped narrowly?
- [ ] Is HTTPS enforced?
- [ ] Are error messages sanitized (no stack traces in responses)?
