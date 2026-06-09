package com.ota.phive;

import com.ota.entity.ValidationFindingEntity.FindingSeverity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhiveValidationServiceTest {

    private final PhiveValidationService service = new PhiveValidationService();

    @Test
    void testValidateMalformedXml() {
        final PhiveValidationResult result = service.validate("<invalid-xml".getBytes());
        assertFalse(result.passed());
        assertFalse(result.findings().isEmpty());
        assertTrue(result.findings().stream().anyMatch(f -> "XSD-FATAL".equals(f.ruleId()) || "XSD-SYSTEM-ERROR".equals(f.ruleId())));
    }

    @Test
    void testValidateValidWellFormedButInvalidUbl() {
        // Valid XML syntax, but not a valid UBL invoice structure/content, so XSD validation will catch errors
        final PhiveValidationResult result = service.validate("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"><cbc:ID xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">INV-001</cbc:ID></Invoice>".getBytes());
        assertFalse(result.passed());
        assertFalse(result.findings().isEmpty());
        assertTrue(result.findings().stream().anyMatch(f -> "XSD-ERROR".equals(f.ruleId())));
    }
}
