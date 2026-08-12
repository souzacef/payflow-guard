package com.carlos.payflowguard.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String FIRST_TEST_SECRET =
            "test-only-first-jwt-signing-key-1234567890";
    private static final String SECOND_TEST_SECRET =
            "test-only-second-jwt-signing-key-0987654321";

    @Test
    void generatesAndValidatesTokenWithSuppliedSigningMaterial() {
        JwtService jwtService = new JwtService(FIRST_TEST_SECRET);

        String token = jwtService.generateToken("user@example.com");

        assertTrue(jwtService.isValid(token));
        assertEquals("user@example.com", jwtService.extractEmail(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentSigningMaterial() {
        JwtService tokenIssuer = new JwtService(FIRST_TEST_SECRET);
        JwtService tokenValidator = new JwtService(SECOND_TEST_SECRET);

        String token = tokenIssuer.generateToken("user@example.com");

        assertFalse(tokenValidator.isValid(token));
    }
}
