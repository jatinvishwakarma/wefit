CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    bio VARCHAR(255),
    gender VARCHAR(255),
    date_of_birth VARCHAR(255),
    profile_pic_url VARCHAR(255),
    role VARCHAR(255) DEFAULT 'USER',
    created_date_time TIMESTAMP(6),
    updated_date_time TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_user_name ON users(user_name);
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_id);
