# 📸 Media Service — File Upload & CDN Integration

> **Module:** `mediaService/`
> **Spring Name:** `media-service`
> **Port:** `8086`
> **Database:** MongoDB (`WefitMediadb`)
> **Last Updated:** 2026-10-02

---

## Purpose

The Media Service handles **file uploads and media management** for Wefit. It:

1. Accepts image/video uploads from users.
2. Stores media metadata in MongoDB.
3. Integrates with cloud storage (S3/GCS) for file persistence.
4. Returns signed URLs for secure media access.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/media/upload` | Upload a media file |
| `GET` | `/api/v1/media/{mediaId}` | Get media metadata + signed URL |
| `DELETE` | `/api/v1/media/{mediaId}` | Delete a media file |

---

## MongoDB Schema

### `media` collection

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | MongoDB ObjectId |
| `userId` | Long | FK to User Service |
| `originalFilename` | String | Original upload filename |
| `contentType` | String | MIME type (image/jpeg, video/mp4) |
| `storageKey` | String | Cloud storage key/path |
| `sizeBytes` | Long | File size |
| `createdAt` | LocalDateTime | Upload timestamp |
