# Skill: Database Operations

## PostgreSQL (UserService)

### Connection Details
- Host: `localhost:5432`
- Database: `wefit`
- Username: `postgres`
- DDL Strategy: Flyway database migrations (ddl-auto: validate)

### Adding a New JPA Entity

1. Create entity class in `entities/` package:
```java
@Entity
@Table(name = "table_name")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use @Column for constraints
    @Column(nullable = false, unique = true)
    private String uniqueField;

    // Use @Enumerated for enums
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MyEnum status = MyEnum.DEFAULT;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdDateTime;

    @UpdateTimestamp
    private LocalDateTime updatedDateTime;
}
```

2. Create repository:
```java
@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    Optional<MyEntity> findByFieldName(String value);
    boolean existsByFieldName(String value);
}
```

### Common Query Method Naming

| Method Name | SQL Equivalent |
|-------------|---------------|
| `findByEmail(String)` | `WHERE email = ?` |
| `findByUserNameOrEmail(String, String)` | `WHERE user_name = ? OR email = ?` |
| `existsByEmail(String)` | `SELECT EXISTS(... WHERE email = ?)` |
| `findById(Long)` | `WHERE id = ?` (inherited) |

---

## MongoDB (ActivityService & AiService)

### Connection Details
- Host: `localhost:27017`
- ActivityService DB: `WefitActivitydb`
- AiService DB: `AiRecommendationsdb`

### Adding a New MongoDB Document

1. Create document class:
```java
@Document("collection_name")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MyDocument {
    @Id
    private String id;  // Always String for MongoDB

    private Long userId;

    @Field("custom_name")  // Rename field in MongoDB
    private Map<String, Object> metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

2. Enable auditing (if not already done):
```java
@Configuration
@EnableMongoAuditing
public class MongoConfigurations {}
```

3. Create repository:
```java
@Repository
public interface MyRepository extends MongoRepository<MyDocument, String> {
    List<MyDocument> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    MyDocument findByActivityId(String activityId);
}
```

### MongoDB Query Method Naming

| Method Name | MongoDB Equivalent |
|-------------|-------------------|
| `findByUserId(Long)` | `{ userId: ? }` |
| `findByActivityId(String)` | `{ activityId: ? }` |
| `findTop5ByUserIdOrderByCreatedAtDesc(Long)` | `{ userId: ? }` sort `{ createdAt: -1 }` limit 5 |

### Important: ID Type

- MongoDB document IDs are **String** (`ObjectId` serialized)
- Always use `MongoRepository<MyDocument, String>`
- **Known bug**: `ActivityRepository` incorrectly uses `Long` as ID type

---

## Cross-Database Referencing

When referencing entities across databases (e.g., userId from PostgreSQL in MongoDB documents):

1. Store the foreign key as a field (e.g., `private Long userId`)
2. Validate existence via REST API call (not DB join)
3. There is **no referential integrity** — handle orphaned references in application code

### Example: Validating a User Before Saving to MongoDB
```java
// In ActivityService
boolean isValidUser = userValidationService.validateUser(userId);
if (!isValidUser) {
    throw new RuntimeException("Invalid user");
}
// Proceed with MongoDB save
```
