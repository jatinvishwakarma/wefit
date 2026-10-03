# Wefit Operational Runbook

## Overview
This document serves as the operational runbook for the Wefit microservices architecture. It contains procedures for deployment, monitoring, scaling, and disaster recovery.

## 1. Deployment and CI/CD
- **Automated Pipeline**: Deployments are handled via GitHub Actions (`.github/workflows/ci.yml`). 
- **Triggers**: Pushes to `main` and `developer` branches trigger tests. Merges to `main` trigger Docker image builds and push to the container registry.
- **Zero-Downtime Deployment**: Kubernetes manifests in `k8s/wefit-stack.yaml` are configured with rolling updates by default.

### To Deploy Manually to Kubernetes
```bash
kubectl apply -f k8s/wefit-stack.yaml
```

## 2. System Monitoring and Observability
- **Metrics**: Prometheus scrapes actuator endpoints from all Spring Boot microservices.
- **Dashboards**: Grafana is accessible at `http://localhost:3000` (admin/admin). Includes JVM metrics, HTTP throughput, and latency.
- **Distributed Tracing**: Zipkin is available at `http://localhost:9411` to trace requests across `api-gateway` -> microservices.
- **Centralized Logging**: Grafana Loki captures container logs, queryable via Grafana's Explore tab.

## 3. Incident Management & Disaster Recovery (DR)

### Scenario A: Database (PostgreSQL/MongoDB) Failure
1. **Symptom**: High error rate (5xx) in `userService`, `activityService`, or `aiService`.
2. **Action**: 
   - Check persistent volumes in Kubernetes (`kubectl get pv,pvc`).
   - Restore from the latest automated snapshot (handled by cloud provider AWS RDS / GCP Cloud SQL in production).

### Scenario B: Kafka Broker Down
1. **Symptom**: Notifications, feed updates, or gamification XP are not updating in real-time.
2. **Action**:
   - Restart Kafka pod: `kubectl rollout restart deployment kafka`
   - Messages are durably stored on disk; once Kafka recovers, consumer lag will spike and then normalize as services catch up.

### Scenario C: High Load / Throttling
1. **Symptom**: High response times, CPU metrics > 80% on Grafana.
2. **Action**: 
   - Kubernetes HPA (Horizontal Pod Autoscaler) should automatically scale replicas.
   - If manual scaling is needed: `kubectl scale deployment <service-name> --replicas=5`

## 4. Security & Compliance
- Ensure `KEYCLOAK_ADMIN_PASSWORD` and `DB_PASSWORD` are injected via Kubernetes Secrets (`wefit-secrets`), NEVER committed to the repo.
- SSL Certificates: Auto-renewed via Let's Encrypt / Cert-Manager in production. Local certs generated via `generate-certs.sh`.

## 5. SLOs (Service Level Objectives)
- **Availability**: 99.9% uptime for API Gateway.
- **Latency**: 95th percentile response time < 200ms.
- **Durability**: 99.999% for user activity data and workout history.
