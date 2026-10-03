# 🤝 Relationship Service — Follow, Unfollow & Social Graph

> **Module:** `relationshipService/`
> **Spring Name:** `relationship-service`
> **Port:** `8085`
> **Database:** MongoDB (`WefitRelationshipdb`)
> **Last Updated:** 2026-10-02

---

## Purpose

The Relationship Service manages the **social graph** of Wefit — who follows whom. It:

1. Handles follow/unfollow actions between users.
2. Exposes followers/following queries.
3. Fires Kafka events on follow/unfollow for the notification service to consume.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/relationships/follow` | Follow a user |
| `DELETE` | `/api/v1/relationships/unfollow` | Unfollow a user |
| `GET` | `/api/v1/relationships/{userId}/followers` | Get a user's followers |
| `GET` | `/api/v1/relationships/{userId}/following` | Get who a user follows |

---

## Kafka Events Published

| Topic | Trigger |
|-------|---------|
| `user-followed` | User follows another user |
| `user-unfollowed` | User unfollows another user |

---

## MongoDB Schema

### `relationships` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `followerId` | Long | User doing the following |
| `followingId` | Long | User being followed |
| `createdAt` | LocalDateTime | Follow timestamp |
