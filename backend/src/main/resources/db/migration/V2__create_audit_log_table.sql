CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NULL,
    timestamp_utc TIMESTAMPTZ NOT NULL DEFAULT now(),
    ip INET NULL,
    user_agent TEXT NULL,
    action VARCHAR(120) NOT NULL,
    result VARCHAR(60) NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT fk_audit_log_user_id
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE SET NULL
);

CREATE INDEX idx_audit_log_user_id ON audit_log (user_id);
CREATE INDEX idx_audit_log_timestamp_utc ON audit_log (timestamp_utc);
CREATE INDEX idx_audit_log_action ON audit_log (action);
