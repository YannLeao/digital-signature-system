CREATE TABLE active_sessions (
    session_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_info TEXT NULL,
    ip VARCHAR(45) NULL,
    user_agent TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_active_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_active_sessions_user_id ON active_sessions (user_id);
CREATE INDEX idx_active_sessions_is_active ON active_sessions (is_active);