ALTER TABLE passkeys
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN last_used DROP NOT NULL,
    ALTER COLUMN last_used TYPE TIMESTAMPTZ USING last_used AT TIME ZONE 'UTC';

CREATE INDEX idx_passkeys_user_id_active
    ON passkeys(user_id, active);