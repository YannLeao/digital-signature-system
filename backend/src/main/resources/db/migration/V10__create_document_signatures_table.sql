CREATE TABLE document_signatures (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    original_hash         VARCHAR(64) NOT NULL,
    signed_hash           VARCHAR(64) NOT NULL,
    signature_id          UUID NOT NULL,
    key_algorithm         VARCHAR(20) NOT NULL,
    seal_page             INTEGER NOT NULL,
    seal_x                NUMERIC(10,2) NOT NULL,
    seal_y                NUMERIC(10,2) NOT NULL,
    origin_ip             VARCHAR(45) NOT NULL,
    signed_at             TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_document_signatures_signature_id UNIQUE (signature_id)
);

CREATE INDEX idx_document_signatures_user_id ON document_signatures(user_id);
CREATE INDEX idx_document_signatures_signed_at ON document_signatures(signed_at);