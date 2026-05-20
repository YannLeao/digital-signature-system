CREATE TABLE passkeys (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    credential_id VARCHAR(512) NOT NULL UNIQUE,
    public_key TEXT NOT NULL,
    counter BIGINT NOT NULL,
    aaguid VARCHAR(36) NOT NULL,
    device_name VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_used TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    CONSTRAINT fk_passkeys_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

