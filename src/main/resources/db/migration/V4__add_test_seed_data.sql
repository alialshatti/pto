-- =============================================================
-- V4: Add seed record for manual validation testing
-- =============================================================
INSERT INTO tdd_receipt (id, bucket_name, object_name, sha256_hash, status)
VALUES (
    'a5b4f2c0-81d3-468b-967c-9b89182390f1',
    'invoices',
    'commercial-invoice-tdd.xml',
    'Ih7GxNGTyvIXFu7ZrL1eCENZ6LfOEW8PWXkWfYmsepk=', -- Base64 SHA-256 of commercial-invoice-tdd.xml
    'RECEIVED'
) ON CONFLICT (id) DO NOTHING;
