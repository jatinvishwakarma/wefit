# WeFit — Troubleshooting

## Service Startup Issues

### Eureka Server won't start

| Symptom | Cause | Fix |
|---------|-------|-----|
| Port 8761 already in use | Another Eureka instance or process running | Kill the process: `netstat -ano | findstr :8761`, then `taskkill /PID <pid> /F` |
| Class not found errors | Spring Cloud version mismatch | Verify `spring-cloud.version` is `2025.1.1` in pom.xml |

### Services fail to register with Eureka

| Symptom | Cause | Fix |
|---------|-------|-----|
| Connection refused to localhost:8761 | Eureka not started yet | Start Eureka first, wait 30s before other services |
| Services show as DOWN | Heartbeat timeout | Check network, increase `eureka.instance.lease-renewal-interval-in-seconds` |

---

## UserService Issues

### "Email already exists" on registration
- **Cause**: Duplicate email in `users` table
- **Check**: `SELECT * FROM users WHERE email = '<email>';`
- **Fix**: Use a different email or delete the existing record

### User not found errors
- **Symptom**: 500 error with "User not found with username or email: ..."
- **Cause**: The `findByUserNameOrEmail` query checks BOTH username AND email fields with the same input
- **Note**: This means if you pass an email, it also checks usernames, and vice versa

### PostgreSQL connection failures
- **Symptom**: `org.postgresql.util.PSQLException: Connection refused`
- **Check**:
  1. PostgreSQL is running on port 5432
  2. Database `wefit` exists: `CREATE DATABASE wefit;`
  3. Credentials match: user=`postgres`, password=`Jatin1307@`
  4. pg_hba.conf allows local connections

### Hibernate DDL issues
- **Config**: `ddl-auto: update` — Hibernate auto-updates schema
- **Issue**: If you rename fields, Hibernate adds new columns but does NOT drop old ones
- **Fix**: Manually drop stale columns or use a migration tool

---

## ActivityService Issues

### "Invalid user" when adding activity
- **Cause 1**: User ID doesn't exist in PostgreSQL
- **Cause 2**: UserService is not running or not reachable
- **Cause 3**: Eureka hasn't registered UserService yet
- **Debug**: Check [UserValidationService.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/service/UserValidationService.java) — any exception returns `false`
- **Fix**: Ensure UserService is running and registered in Eureka dashboard

### WebClient "user-service" not resolved
- **Symptom**: `java.net.UnknownHostException: user-service`
- **Cause**: Eureka lookup failing or `@LoadBalanced` not applied
- **Fix**: Verify `@LoadBalanced` on `WebClient.Builder` bean in [WebClientConfig.java](file:///c:/Wefit/activityService/src/main/java/com/wefit/activityService/config/WebClientConfig.java)
- **Note**: The base URL uses `http://user-service` but the Eureka service name is `userService`. Eureka is case-insensitive, but be aware.

### MongoDB connection issues
- **Symptom**: `com.mongodb.MongoSocketOpenException`
- **Check**: MongoDB running on port 27017
- **Fix**: Start MongoDB: `mongod` or restart MongoDB service

---

## AiService Issues

### Kafka consumer not receiving messages
- **Check list**:
  1. Kafka broker running on `localhost:9092`
  2. Topic `activity-events` exists
  3. Consumer group ID matches: `activity-processor-group`
  4. Check deserialization config: `spring.json.value.default.type` should be `com.wefit.aiService.entities.Activity`
  5. Check `spring.json.trusted.packages` is `*`
- **Create topic manually**: `kafka-topics --create --topic activity-events --bootstrap-server localhost:9092`

### Deserialization errors
- **Symptom**: `org.apache.kafka.common.errors.SerializationException`
- **Cause**: Mismatch between producer Activity class and consumer Activity class
- **Note**: Both services have their own `Activity` class. Fields must match for JSON deserialization
- **Fix**: Ensure `com.wefit.aiService.entities.Activity` fields match the JSON produced by `com.wefit.activityService.entities.Activity`

### Missing Kafka producer in ActivityService
- **Important**: As of current code, ActivityService has Kafka dependencies and configuration but does NOT actually publish messages to Kafka. The `KafkaTemplate.send()` call is **not implemented** in `ActivityService.java`. This means the AiService Kafka consumer will never receive events.

---

## Build Issues

### Lombok compilation errors
- **Symptom**: "cannot find symbol" for getters/setters
- **Fix**: Ensure annotation processing is enabled in IDE
  - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
- **Maven**: The `maven-compiler-plugin` is configured with Lombok annotation processor paths

### Maven wrapper issues
- **Symptom**: `mvnw.cmd` not executable or not found
- **Fix**: Run from the service directory (e.g., `cd userService && mvnw.cmd spring-boot:run`)

---

## Common Development Scenarios

### Clean restart all services
```bash
# Kill all Java processes
taskkill /F /IM java.exe

# Restart infrastructure
# (ensure PostgreSQL, MongoDB, Kafka are running)

# Start services in order
start-services.bat
```

### Reset database
```sql
-- PostgreSQL
DROP DATABASE wefit;
CREATE DATABASE wefit;
-- Hibernate will recreate tables on next start

-- MongoDB
use WefitActivitydb;
db.dropDatabase();
use AiRecommendationsdb;
db.dropDatabase();
```
