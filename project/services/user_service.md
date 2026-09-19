# 👤 User Service — Registration, Profiles & Validation

> **Module:** `userService/`
> **Spring Name:** `user-service`
> **Port:** `8081`
> **Database:** PostgreSQL (`wefit`)
> **Last Updated:** 2026-09-19

---

## Purpose

The User Service is the **identity backbone** of the Wefit platform. It handles:

- User registration (both direct and Keycloak-synced).
- User profile retrieval (by ID, email/username, or Keycloak ID).
- User existence validation (used by Activity Service before logging workouts).
- Password hashing with BCrypt.

---

## Directory Structure

```
userService/
├── src/main/java/com/wefit/userService/
│   ├── UserServiceApplication.java         ← Main Spring Boot class
│   ├── controller/
│   │   ├── AuthController.java             ← Registration + validation endpoints
│   │   └── UserController.java             ← Profile/query endpoints
│   ├── dto/
│   │   ├── UserRequestDto.java             ← Input DTO with validation annotations
│   │   └── UserResponseDto.java            ← Output DTO (excludes password)
│   ├── entities/
│   │   ├── User.java                       ← JPA entity (mapped to "users" table)
│   │   └── UserRole.java                   ← Enum: USER, ADMIN, COACH
│   ├── exception/
│   │   └── ResourceNotFoundException.java  ← 404 exception with @ResponseStatus
│   ├── repository/
│   │   └── UserRepository.java             ← JPA repository with custom queries
│   └── service/
│       └── UserService.java                ← Business logic
├── src/main/resources/
│   └── application.yml                     ← Bootstrap to Config Server
├── src/test/
│   ├── java/.../UserServiceApplicationTests.java
│   └── resources/application-test.yml
└── pom.xml
```

---

## Database Schema

**Database:** PostgreSQL (`wefit`)
**Table:** `users`
**ORM:** Spring Data JPA / Hibernate with `ddl-auto: update` (schema auto-created/updated on startup)

| Column              | Type             | Constraints                 | Notes                                    |
|---------------------|------------------|-----------------------------|------------------------------------------|
| `id`                | `BIGSERIAL`      | Primary Key, auto-generated | `GenerationType.IDENTITY`                |
| `keycloak_id`       | `VARCHAR`        | Unique                      | Links to Keycloak subject (`sub` claim)  |
| `first_name`        | `VARCHAR`        | NOT NULL                    |                                          |
| `last_name`         | `VARCHAR`        | NOT NULL                    |                                          |
| `user_name`         | `VARCHAR`        | Unique, NOT NULL            |                                          |
| `email`             | `VARCHAR`        | Unique, NOT NULL            |                                          |
| `password`          | `VARCHAR`        | NOT NULL                    | BCrypt hashed. `"KEYCLOAK_MANAGED"` for Keycloak users |
| `phone_number`      | `VARCHAR`        | Nullable                    |                                          |
| `bio`               | `VARCHAR`        | Nullable                    |                                          |
| `gender`            | `VARCHAR`        | Nullable                    |                                          |
| `date_of_birth`     | `VARCHAR`        | Nullable                    | Stored as string (not date)              |
| `profile_pic_url`   | `VARCHAR`        | Nullable                    |                                          |
| `role`              | `VARCHAR`        | Default: `USER`             | Enum stored as string (`@Enumerated(EnumType.STRING)`) |
| `created_date_time` | `TIMESTAMP`      | Auto-set on insert          | `@CreationTimestamp`                     |
| `updadated_date_time` | `TIMESTAMP`    | Auto-set on update          | `@UpdateTimestamp` (note: typo in field name is in the code) |

---

## Entity: User.java

**Path:** `userService/src/main/java/com/wefit/userService/entities/User.java`

The `User` entity uses:
- **Lombok:** `@Data` (getters/setters), `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Hibernate auditing:** `@CreationTimestamp`, `@UpdateTimestamp` for automatic timestamp management.
- **Static factory methods:**
  - `User.fromEntity(UserRequestDto)` — Converts a request DTO to a `User` entity.
  - `User.toDto(User)` — Converts a `User` entity to a `UserResponseDto`.

### UserRole Enum

```java
public enum UserRole { USER, ADMIN, COACH }
```

Stored as a string in the database via `@Enumerated(EnumType.STRING)`. Default value is `USER` (set via `@Builder.Default`).

---

## DTOs

### UserRequestDto (Input)

| Field          | Type       | Validation                                                                     |
|----------------|-----------|---------------------------------------------------------------------------------|
| `keycloakId`   | `String`  | None (set by Gateway's sync filter)                                            |
| `firstName`    | `String`  | `@NotBlank("First name is required")`                                          |
| `lastName`     | `String`  | `@NotBlank("Last name is required")`                                           |
| `userName`     | `String`  | `@NotBlank("Username is required")`                                            |
| `email`        | `String`  | `@NotBlank`, `@Email("Email is not valid")`                                    |
| `password`     | `String`  | `@NotBlank`, `@Pattern` (min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char) |
| `phoneNumber`  | `String`  | None                                                                           |
| `bio`          | `String`  | None                                                                           |
| `gender`       | `String`  | None                                                                           |
| `dateOfBirth`  | `String`  | None                                                                           |
| `role`         | `UserRole`| None (defaults to `USER` in service layer)                                     |

**Password regex:** `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$`

### UserResponseDto (Output)

Same fields as User entity **except `password`** — passwords are never returned to the client.

Has a static `toDto(User)` factory method that mirrors the entity's conversion logic.

---

## API Endpoints

### AuthController (`/api/user/auth`)

| Method | Path                            | Description                        | Request Body           | Response               |
|--------|----------------------------------|------------------------------------|------------------------|------------------------|
| `POST` | `/api/user/auth/register`       | Register a new user                | `UserRequestDto` (`@Valid`) | `UserResponseDto` (200) |
| `GET`  | `/api/user/auth/{userId}/validate` | Check if user exists by ID      | —                      | `Boolean` (200)        |

### UserController (`/api/users`)

| Method | Path                                 | Description                              | Response               |
|--------|--------------------------------------|------------------------------------------|------------------------|
| `POST` | `/api/users/register`               | Register (duplicate route for Gateway)   | `UserResponseDto` (200) |
| `GET`  | `/api/users/keycloak/{keycloakId}`  | Look up user by Keycloak ID              | `UserResponseDto` (200) |
| `GET`  | `/api/users/profile/{identifier}`   | Look up by username OR email             | `UserResponseDto` (200) |
| `GET`  | `/api/users/{id}`                   | Look up by internal Long ID              | `UserResponseDto` (200) |

**Why two controllers?**
- `AuthController` (`/api/user/auth`) handles authentication-related actions (register, validate).
- `UserController` (`/api/users`) handles profile queries and Keycloak-specific lookups.
- The Activity Service calls `AuthController` for validation; the Gateway calls `UserController` for Keycloak sync.

---

## Service Layer: UserService.java

**Path:** `userService/src/main/java/com/wefit/userService/service/UserService.java`

### `registerUser(UserRequestDto)`

This is the most complex method. Here's the complete flow:

```
1. Check if a user with this email already exists
   ↓
2a. EXISTS + no keycloakId → Link the Keycloak account (set keycloakId, save, return)
2b. EXISTS + same keycloakId → Return existing user (idempotent)
2c. EXISTS + different keycloakId → Throw "Email already linked to another Keycloak account"
   ↓
3. NEW USER:
   a. Build User entity from DTO
   b. Hash password with BCryptPasswordEncoder
   c. Default role to USER if not provided
   d. Set createdDateTime and updadatedDateTime
   e. Save to PostgreSQL
   f. Convert to UserResponseDto and return
```

**Why this logic?** The Gateway's `KeyCloakUserSyncFilter` may call this endpoint when a Keycloak-authenticated user isn't found in the database. If a user was previously registered directly (without Keycloak), we want to **link** their existing account to the Keycloak identity rather than creating a duplicate.

### Other Methods

| Method                    | What it does                                              |
|---------------------------|-----------------------------------------------------------|
| `getUserProfile(identifier)` | Finds by username OR email using `findByUserNameOrEmail` |
| `getUserById(id)`         | Finds by Long ID, throws `ResourceNotFoundException` if missing |
| `getUserByKeycloakId(keycloakId)` | Finds by Keycloak subject ID                     |
| `existsById(id)`          | Returns `boolean` — used by Activity Service's validation |

---

## Repository: UserRepository.java

**Path:** `userService/src/main/java/com/wefit/userService/repository/UserRepository.java`

Extends `JpaRepository<User, Long>` with custom query methods:

| Method                                    | Generated SQL (by Spring Data JPA)                          |
|-------------------------------------------|------------------------------------------------------------|
| `findByUserNameOrEmail(userName, email)`  | `SELECT * FROM users WHERE user_name = ? OR email = ?`    |
| `findByEmail(email)`                      | `SELECT * FROM users WHERE email = ?`                     |
| `findByKeycloakId(keycloakId)`           | `SELECT * FROM users WHERE keycloak_id = ?`               |
| `existsByEmail(email)`                    | `SELECT COUNT(1) > 0 FROM users WHERE email = ?`          |
| `existsByKeycloakId(keycloakId)`         | `SELECT COUNT(1) > 0 FROM users WHERE keycloak_id = ?`    |
| `existsById(id)`                          | `SELECT COUNT(1) > 0 FROM users WHERE id = ?`             |

---

## Exception: ResourceNotFoundException

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
```

Annotated with `@ResponseStatus(HttpStatus.NOT_FOUND)` so Spring automatically returns a `404` HTTP response when this exception is thrown. This is **critical** because the Gateway's `KeyCloakUserSyncFilter` specifically catches `WebClientResponseException.NotFound` (404) to trigger auto-registration.

---

## Configuration

From Config Server (`config/user-service.yml`):

| Property                        | Value                                           |
|---------------------------------|-------------------------------------------------|
| `spring.datasource.url`        | `${DB_URL_USER_SERVICE:jdbc:postgresql://...}`  |
| `spring.datasource.username`   | `${DB_USERNAME:postgres}`                       |
| `spring.datasource.password`   | `${DB_PASSWORD}`                                |
| `spring.jpa.hibernate.ddl-auto` | `update` (auto-create/modify tables)           |
| `spring.jpa.show-sql`          | `true` (SQL logged to console)                  |
| `server.port`                   | `8081`                                          |
| SSL                             | Enabled (PKCS12 keystore)                       |

---

## Design Decisions

1. **PostgreSQL (not MongoDB):** User data is highly relational and requires strict uniqueness constraints (email, username). PostgreSQL with JPA is ideal for this.
2. **BCrypt password hashing:** Industry standard for password storage. The `BCryptPasswordEncoder` is instantiated inline (not as a `@Bean`), which works but could be improved by injecting it via Spring's security configuration.
3. **`ddl-auto: update`:** Great for development. For production, this should be switched to `validate` and migrations managed with Flyway or Liquibase.
4. **No global error handler:** Exceptions are handled via `@ResponseStatus` annotations. A `@ControllerAdvice` could provide more consistent error responses in the future.
