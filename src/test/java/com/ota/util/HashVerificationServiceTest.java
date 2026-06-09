package com.ota.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashVerificationServiceTest {

    private final HashVerificationService service = new HashVerificationService();

    @Test
    void testVerifyValidHash() throws Exception {
        byte[] data = "test invoice data".getBytes();
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        String expectedHash = Base64.getEncoder().encodeToString(digest.digest(data));

        assertTrue(service.verify(data, expectedHash));
    }

    @Test
    void testVerifyInvalidHash() {
        byte[] data = "test invoice data".getBytes();
        String invalidHash = Base64.getEncoder().encodeToString("wrong hash".getBytes());

        assertFalse(service.verify(data, invalidHash));
    }

    @Test
    void testVerifyNullData() {
        assertFalse(service.verify(null, "somehash"));
    }
}
