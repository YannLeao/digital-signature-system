CREATE TABLE user_keys (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    public_key            TEXT NOT NULL,
    encrypted_private_key TEXT NOT NULL,
    key_algorithm         VARCHAR(20) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_user_keys_user_id UNIQUE (user_id)
);

CREATE INDEX idx_user_keys_user_id ON user_keys(user_id);