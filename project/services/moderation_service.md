# 🚨 Moderation Service — Content Safety & Report Management

> **Module:** `moderationService/`
> **Spring Name:** `moderation-service`
> **Port:** `8087`
> **Database:** MongoDB (`WefitModerationdb`)
> **Last Updated:** 2026-10-02

---

## Purpose

The Moderation Service handles **user-reported content safety** for the Wefit platform. It:

1. Accepts content reports from users (posts, comments, activities).
2. Auto-hides content after reaching a configurable threshold (default: 3 reports).
3. Allows moderation actions (REMOVE, RESTORE, DISMISS) via admin endpoints.
4. Publishes Kafka events for all major moderation state changes.

---

## Directory Structure

```
moderationService/
├── src/main/java/com/wefit/moderationService/
│   ├── ModerationServiceApplication.java
│   ├── controller/
│   │   └── ModerationController.java         ← REST API
│   ├── dto/
│   │   ├── ReportRequest.java                ← Report submission DTO
│   │   └── ModerationActionRequest.java      ← Admin action DTO
│   ├── entities/
│   │   ├── Report.java                       ← MongoDB: report document
│   │   └── ContentState.java                 ← Enum: VISIBLE, HIDDEN, REMOVED, RESTORED
│   ├── repositories/
│   │   └── ReportRepository.java             ← MongoDB repository
│   └── service/
│       └── ModerationService.java            ← Core moderation logic
└── pom.xml
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/moderation/report` | Submit a content report |
| `GET` | `/api/v1/moderation/reports` | List all pending reports (admin) |
| `POST` | `/api/v1/moderation/action` | Take moderation action (REMOVE/RESTORE/DISMISS) |

---

## MongoDB Schema

### `reports` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `contentId` | String | ID of reported content |
| `contentType` | String | e.g. "POST", "COMMENT" |
| `reportedByUserId` | Long | User who filed the report |
| `reason` | String | Reason for report |
| `contentState` | ContentState | VISIBLE / HIDDEN / REMOVED / RESTORED |
| `createdAt` | LocalDateTime | Report timestamp |

---

## Kafka Events Published

| Topic | Trigger | Payload |
|-------|---------|---------|
| `content-moderated` | REMOVED action | `{contentId, contentType, action: "REMOVED"}` |
| `content-moderated` | RESTORED action | `{contentId, contentType, action: "RESTORED"}` |

---

## Design Decisions

1. **Auto-hide at 3 reports**: Content with 3+ reports is automatically set to `HIDDEN` state pending moderation review. This reduces harm quickly without requiring a human in the loop for every report.
2. **State machine (ContentState enum)**: Content progresses through states: `VISIBLE → HIDDEN → REMOVED` (or `REMOVED → RESTORED`). This prevents invalid transitions.
3. **Separate moderation topic**: Using a dedicated `content-moderated` Kafka topic (rather than the general `activity-events`) allows the notification service to subscribe independently and alert affected users.
