-- =============================================================
-- V2: Create schematron_validation_run table
-- One row per validation execution against a tdd_receipt
-- =============================================================
CREATE TABLE schematron_validation_run
(
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    tdd_receipt_id     UUID         NOT NULL,
    validation_status  VARCHAR(50)  NOT NULL DEFAULT 'IN_PROGRESS',
    finding_count      INT          NOT NULL DEFAULT 0,
    error_count        INT          NOT NULL DEFAULT 0,
    warning_count      INT          NOT NULL DEFAULT 0,
    report_object_name VARCHAR(500) NULL, -- path in MinIO: validation-reports/{runId}.svrl.xml
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at       TIMESTAMP    NULL,

    CONSTRAINT PK_schematron_validation_run PRIMARY KEY (id),
    CONSTRAINT FK_validation_run_receipt
        FOREIGN KEY (tdd_receipt_id) REFERENCES tdd_receipt (id),
    CONSTRAINT CHK_validation_run_status
        CHECK (validation_status IN ('IN_PROGRESS', 'PASSED', 'FAILED', 'HASH_MISMATCH', 'STORAGE_ERROR'))
);

CREATE INDEX IDX_validation_run_receipt_id ON schematron_validation_run (tdd_receipt_id);
CREATE INDEX IDX_validation_run_status ON schematron_validation_run (validation_status);
