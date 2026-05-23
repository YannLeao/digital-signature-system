CREATE TABLE jwt_denylist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jti VARCHAR(64) NOT NULL,
    user_id UUID NULL,
    session_id UUID NOT NULL,
    token_expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(32) NOT NULL,
    CONSTRAINT fk_jwt_denylist_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE SET NULL,
    CONSTRAINT uk_jwt_denylist_jti UNIQUE (jti),
    CONSTRAINT chk_jwt_denylist_reason CHECK (reason IN ('LOGOUT'))
);

CREATE INDEX idx_jwt_denylist_jti ON jwt_denylist (jti);
CREATE INDEX idx_jwt_denylist_token_expires_at ON jwt_denylist (token_expires_at);
CREATE INDEX idx_jwt_denylist_session_id ON jwt_denylist (session_id);
