ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash TYPE VARCHAR(64),
    ALTER COLUMN created_by_ip_hash TYPE VARCHAR(64),
    ALTER COLUMN created_by_user_agent_hash TYPE VARCHAR(64);
