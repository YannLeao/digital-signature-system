ALTER TABLE users
    ADD COLUMN totp_secret_encrypted  TEXT          NULL,
    ADD COLUMN totp_enabled           BOOLEAN       NOT NULL DEFAULT FALSE,
    ADD COLUMN totp_failed_attempts   INTEGER       NOT NULL DEFAULT 0,
    ADD COLUMN totp_locked_until      TIMESTAMPTZ   NULL;

CREATE TABLE totp_backup_codes (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    code_hash  VARCHAR(255) NOT NULL,
    used_at    TIMESTAMPTZ  NULL,
    CONSTRAINT fk_totp_backup_codes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_totp_backup_codes_user_id ON totp_backup_codes(user_id);