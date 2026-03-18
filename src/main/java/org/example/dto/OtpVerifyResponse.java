package org.example.dto;

import java.time.Instant;

public class OtpVerifyResponse {

    private final String sessionToken;
    private final Instant expiresAt;

    public OtpVerifyResponse(String sessionToken, Instant expiresAt) {
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}

