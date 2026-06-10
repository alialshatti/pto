-- =============================================================
-- V4: Add seed record for manual validation testing
-- =============================================================
INSERT INTO tdd_receipt (id, bucket_name, object_name, sha256_hash, status)
VALUES (
    'd601f409-5a50-482f-871d-f8df609825b4',
    'invoices',
    'commercial-invoice-tdd.xml',
    'Ih7GxNGTyvIXFu7ZrL1eCENZ6LfOEW8PWXkWfYmsepk=', -- Base64 SHA-256 of commercial-invoice-tdd.xml
    'RECEIVED'
) ON CONFLICT (id) DO NOTHING;
