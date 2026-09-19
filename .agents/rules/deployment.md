# WeFit — Deployment

## Local Development Setup

### Prerequisites

| Component    | Version  | Port  | Install                                      |
|-------------|----------|-------|----------------------------------------------|
| Java JDK    | 21+      | —     | [Adoptium](https://adoptium.net/)            |
| PostgreSQL  | 12+      | 5432  | [postgresql.org](https://www.postgresql.org/) |
| MongoDB     | 6+       | 27017 | [mongodb.com](https://www.mongodb.com/)      |
| Apache Kafka| 3+       | 9092  | [kafka.apache.org](https://kafka.apache.org/) |

### Database Setup

#### PostgreSQL
```sql
-- Create database (Hibernate auto-creates tables)
CREATE DATABASE wefit;
```

Current credentials (hardcoded in application.yml):
- **User**: `postgres`
- **Password**: `Jatin1307@`

#### MongoDB
No explicit setup needed — databases and collections are auto-created on first write:
- `WefitActivitydb` → collection `Activities`
- `AiRecommendationsdb` → collection `ai_recommendations`

#### Kafka
```bash
# Start Zookeeper (if using older Kafka)
bin\windows\zookeeper-server-start.bat config\zookeeper.properties

# Start Kafka broker
bin\windows\kafka-server-start.bat config\server.properties

# Create the topic (optional — auto-created by Spring Kafka)
bin\windows\kafka-topics.bat --create --topic activity-events --bootstrap-server localhost:9092
```

### Starting Services

#### Option 1: Batch Script (Windows)
```bash
cd c:\Wefit
start-services.bat
```

#### Option 2: Python Script (Cross-platform)
```bash
cd c:\Wefit
python start_services.py
```

#### Option 3: Manual (per-service)
```bash
# Terminal 1 — Eureka (start first, wait 30s)
cd eureka && mvnw.cmd spring-boot:run

# Terminal 2 — UserService
cd userService && mvnw.cmd spring-boot:run

# Terminal 3 — ActivityService
cd activityService && mvnw.cmd spring-boot:run

# Terminal 4 — AiService
cd aiService && mvnw.cmd spring-boot:run
```

### Verifying Services

| Check | URL |
|-------|-----|
| Eureka dashboard | http://localhost:8761 |
| User registration | POST http://localhost:8081/api/user/auth/register |
| User validation | GET http://localhost:8081/api/user/auth/1/validate |
| Add activity | POST http://localhost:8082/api/activities/add |
| Get recommendations | GET http://localhost:8083/api/recommendations/user/1 |

---

## Build Commands

### Build a single service
```bash
cd <service>
mvnw.cmd clean package
```

### Build without tests
```bash
cd <service>
mvnw.cmd clean package -DskipTests
```

### Run a single service
```bash
cd <service>
mvnw.cmd spring-boot:run
```

---

## Production Deployment (TODO)

The following are NOT yet configured but would be needed for production:

### Configuration Externalization
- [ ] Use environment variables or Spring Cloud Config Server for secrets
- [ ] Remove hardcoded database credentials from `application.yml`
- [ ] Create `application-prod.yml` profiles

### Containerization
- [ ] Add `Dockerfile` to each service
- [ ] Create `docker-compose.yml` for full stack
- [ ] Include PostgreSQL, MongoDB, Kafka, Zookeeper containers

### Security
- [ ] Add Spring Security with JWT authentication
- [ ] Implement password hashing (BCrypt)
- [ ] Add API Gateway (Spring Cloud Gateway)
- [ ] Implement CORS configuration

### Monitoring & Observability
- [ ] Add Spring Boot Actuator to all services
- [ ] Integrate distributed tracing (Micrometer / Zipkin)
- [ ] Set up centralized logging (ELK / Loki)
- [ ] Add health checks for Eureka

### CI/CD
- [ ] Add GitHub Actions workflows for build/test
- [ ] Set up Docker image publishing
- [ ] Implement multi-stage Docker builds

---

## Infrastructure Topology

### Local Development
```
localhost
├── :5432  PostgreSQL (wefit DB)
├── :9092  Kafka Broker
├── :27017 MongoDB
├── :8761  Eureka Server
├── :8081  UserService
├── :8082  ActivityService
└── :8083  AiService
```

### Suggested Production Architecture
```
┌──────────────┐
│  API Gateway │  (Spring Cloud Gateway / Kong / NGINX)
└──────┬───────┘
       │
┌──────┴───────────────────────────────────┐
│           Service Mesh / Eureka          │
├──────────┬──────────────┬────────────────┤
│ UserSvc  │ ActivitySvc  │   AiService    │
│ (N pods) │  (N pods)    │   (N pods)     │
├──────────┴──────────────┴────────────────┤
│     PostgreSQL    MongoDB    Kafka       │
└──────────────────────────────────────────┘
```
