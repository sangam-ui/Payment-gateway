package org.example.service;

import org.example.api.ErrorCode;
import org.example.dto.OtpChallengeResponse;
import org.example.dto.OtpVerifyResponse;
import org.example.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpAuthService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpChallenge> challenges = new ConcurrentHashMap<String, OtpChallenge>();
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<String, SessionInfo>();

    public OtpChallengeResponse requestOtp(String username, String phone) {
        String challengeId = UUID.randomUUID().toString();
        String otp = String.format("%06d", Integer.valueOf(secureRandom.nextInt(1000000)));
        Instant expiresAt = Instant.now().plus(OTP_TTL);
        challenges.put(challengeId, new OtpChallenge(username, phone, otp, expiresAt));
        return new OtpChallengeResponse(challengeId, otp, expiresAt);
    }

    public OtpVerifyResponse verifyOtp(String username, String challengeId, String otp) {
        OtpChallenge challenge = challenges.get(challengeId);
        if (challenge == null || !challenge.getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.OTP_INVALID, "OTP challenge is invalid");
        }
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            challenges.remove(challengeId);
            throw new BusinessException(ErrorCode.OTP_EXPIRED, "OTP has expired");
        }
        if (!challenge.getOtp().equals(otp)) {
            throw new BusinessException(ErrorCode.OTP_INVALID, "OTP is incorrect");
        }

        challenges.remove(challengeId);
        String sessionToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(SESSION_TTL);
        sessions.put(sessionToken, new SessionInfo(username, challenge.getPhone(), expiresAt));
        return new OtpVerifyResponse(sessionToken, expiresAt);
    }

    public boolean isValidSession(String username, String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }
        SessionInfo session = sessions.get(sessionToken);
        if (session == null) {
            return false;
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionToken);
            return false;
        }
        return session.getUsername().equals(username);
    }

    private static final class OtpChallenge {
        private final String username;
        private final String phone;
        private final String otp;
        private final Instant expiresAt;

        private OtpChallenge(String username, String phone, String otp, Instant expiresAt) {
            this.username = username;
            this.phone = phone;
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        private String getUsername() {
            return username;
        }

        private String getPhone() {
            return phone;
        }

        private String getOtp() {
            return otp;
        }

        private Instant getExpiresAt() {
            return expiresAt;
        }
    }

    private static final class SessionInfo {
        private final String username;
        private final String phone;
        private final Instant expiresAt;

        private SessionInfo(String username, String phone, Instant expiresAt) {
            this.username = username;
            this.phone = phone;
            this.expiresAt = expiresAt;
        }

        private String getUsername() {
            return username;
        }

        @SuppressWarnings("unused")
        private String getPhone() {
            return phone;
        }

        private Instant getExpiresAt() {
            return expiresAt;
        }
    }
}

