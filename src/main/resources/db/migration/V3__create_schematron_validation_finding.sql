-- =============================================================
-- V3: Create schematron_validation_finding table
-- Stores individual validation issues per validation run
-- =============================================================
CREATE TABLE schematron_validation_finding
(
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    validation_run_id UUID         NOT NULL,
    rule_id           VARCHAR(100) NULL,  -- e.g. OM-BR-001
    message           TEXT         NOT NULL,
    severity          VARCHAR(20)  NOT NULL, -- ERROR | WARNING | INFO
    source_xpath      TEXT         NULL,  -- XPath location from SVRL
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_schematron_validation_finding PRIMARY KEY (id),
    CONSTRAINT FK_finding_validation_run
        FOREIGN KEY (validation_run_id) REFERENCES schematron_validation_run (id),
    CONSTRAINT CHK_finding_severity
        CHECK (severity IN ('ERROR', 'WARNING', 'INFO'))
);

CREATE INDEX IDX_finding_run_id ON schematron_validation_finding (validation_run_id);
CREATE INDEX IDX_finding_severity ON schematron_validation_finding (validation_run_id, severity);
