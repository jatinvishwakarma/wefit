CREATE TABLE comments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_post_id ON comments(post_id);

CREATE TABLE likes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    target_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_likes_target ON likes(target_type, target_id);
CREATE UNIQUE INDEX idx_likes_unique ON likes(user_id, target_id, target_type);
