# Skill: Code Review

## Code Review Checklist for WeFit

### General
- [ ] Does the code follow existing patterns in the codebase?
- [ ] Are Lombok annotations used consistently (`@Data`, `@Builder`, `@RequiredArgsConstructor`)?
- [ ] Is constructor injection used (no `@Autowired` field injection)?
- [ ] Are DTOs used for controller input/output (no raw entities)?
- [ ] Are response types wrapped in `ResponseEntity<T>`?

### Entities & DTOs
- [ ] JPA entities: `@Entity`, `@Table`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- [ ] MongoDB documents: `@Document`, `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- [ ] ID field type matches repository generic parameter
- [ ] Timestamps use `@CreationTimestamp`/`@UpdateTimestamp` (JPA) or `@CreatedDate`/`@LastModifiedDate` (MongoDB)
- [ ] Static `toDto()` method exists on response DTOs

### Controllers
- [ ] `@RestController` + `@RequestMapping` with leading `/`
- [ ] `@Valid` on request body parameters
- [ ] Appropriate HTTP methods (GET for reads, POST for creates)
- [ ] Returns `ResponseEntity.ok()` for success

### Services
- [ ] `@Service` annotation
- [ ] `@RequiredArgsConstructor` for DI
- [ ] All dependencies are `private final`
- [ ] Business logic is in service layer (not controller)
- [ ] Proper error handling (not swallowing exceptions silently)

### Repositories
- [ ] `@Repository` annotation
- [ ] Extends correct base: `JpaRepository<Entity, Long>` or `MongoRepository<Document, String>`
- [ ] Query method naming follows Spring Data conventions
- [ ] Custom queries documented with comments

### Configuration
- [ ] No hardcoded secrets in `application.yml`
- [ ] Eureka client configuration present
- [ ] Kafka serializer/deserializer configuration correct
- [ ] MongoDB auditing enabled if using `@CreatedDate`/`@LastModifiedDate`

### Known Anti-Patterns to Flag

| Anti-Pattern | Where Found | Why It's Bad |
|-------------|-------------|--------------|
| `throw new RuntimeException("...")` | All services | No proper HTTP status codes |
| Plaintext passwords | UserService | Security vulnerability |
| Hardcoded DB credentials | UserService YAML | Security vulnerability |
| Entity exposed in API response | AiService controller | Couples internal model to API contract |
| Unused imports | ActivityMessageListener | Code cleanliness |
| `MongoRepository<Activity, Long>` | ActivityRepository | ID type mismatch (should be String) |
| Duplicate conversion logic | User.java + UserResponseDto.java | DRY violation |

### Documentation Checklist

After approving a code change, ensure:
- [ ] `.claude/api-reference.md` updated (if endpoints changed)
- [ ] `.claude/database-schema.md` updated (if entities/schema changed)
- [ ] `.claude/workflows.md` updated (if business flow changed)
- [ ] `.claude/changelog.md` updated (always)
- [ ] `.claude/architecture.md` updated (if structural changes)
