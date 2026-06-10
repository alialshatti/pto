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
    void testValidateValidWellFormedButInvalidTaxData() {
        // Valid XML syntax, but not a valid TaxData structure/content, so XSD validation will catch errors
        final PhiveValidationResult result = service.validate("<TaxData xmlns=\"urn:peppol:schema:om-taxdata:1.0\"><cbc:CustomizationID xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">urn:peppol:taxdata:om-1</cbc:CustomizationID></TaxData>".getBytes());
        assertFalse(result.passed());
        assertFalse(result.findings().isEmpty());
        assertTrue(result.findings().stream().anyMatch(f -> "XSD-ERROR".equals(f.ruleId())));
    }
}
