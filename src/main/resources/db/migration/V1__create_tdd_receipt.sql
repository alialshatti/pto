-- =============================================================
-- V1: Create tdd_receipt table
-- Stores incoming TDD invoice receipts with OCI/MinIO references
-- =============================================================
CREATE TABLE tdd_receipt
(
    id          UUID          NOT NULL DEFAULT gen_random_uuid(),
    bucket_name VARCHAR(255)  NOT NULL,
    object_name VARCHAR(500)  NOT NULL,
    sha256_hash VARCHAR(128)  NOT NULL, -- Base64-encoded SHA-256 of the XML
    status      VARCHAR(50)   NOT NULL DEFAULT 'RECEIVED',
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_tdd_receipt PRIMARY KEY (id),
    CONSTRAINT CHK_tdd_receipt_status
        CHECK (status IN ('RECEIVED', 'VALIDATING', 'VALIDATED', 'VALIDATION_FAILED'))
);

CREATE INDEX IDX_tdd_receipt_status ON tdd_receipt (status);
