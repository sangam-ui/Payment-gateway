package org.example.dto;

import java.time.Instant;

public class OtpChallengeResponse {

    private final String challengeId;
    private final String otp;
    private final Instant expiresAt;

    public OtpChallengeResponse(String challengeId, String otp, Instant expiresAt) {
        this.challengeId = challengeId;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public String getOtp() {
        return otp;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}

