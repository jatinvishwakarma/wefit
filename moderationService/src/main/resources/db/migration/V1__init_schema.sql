CREATE TABLE IF NOT EXISTS reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    content_id VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    state VARCHAR(255) NOT NULL,
    reviewer_notes VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6)
);
