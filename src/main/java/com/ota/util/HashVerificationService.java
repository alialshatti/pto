package com.ota.util;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class HashVerificationService {

    public boolean verify(byte[] data, String expectedBase64Hash) {
        if (data == null || expectedBase64Hash == null) {
            return false;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            String calculatedBase64Hash = Base64.getEncoder().encodeToString(hashBytes);
            return calculatedBase64Hash.equals(expectedBase64Hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
