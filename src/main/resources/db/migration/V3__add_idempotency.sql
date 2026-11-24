
ALTER TABLE file_metadata
ADD COLUMN idempotency_key VARCHAR(255),
ADD COLUMN request_fingerprint VARCHAR(64);

ALTER TABLE file_metadata
ADD CONSTRAINT uq_file_org_idempotency
UNIQUE (organization_id, idempotency_key);
