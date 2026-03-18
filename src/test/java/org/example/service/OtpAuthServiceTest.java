package org.example.service;

import org.example.api.ErrorCode;
import org.example.dto.OtpChallengeResponse;
import org.example.dto.OtpVerifyResponse;
import org.example.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpAuthServiceTest {

    private final OtpAuthService otpAuthService = new OtpAuthService();

    @Test
    void shouldRejectUnknownChallenge() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> otpAuthService.verifyOtp("user", "missing-challenge", "123456"));

        assertEquals(ErrorCode.OTP_INVALID, ex.getErrorCode());
    }

    @Test
    void shouldRejectWrongOtpForExistingChallenge() {
        OtpChallengeResponse challenge = otpAuthService.requestOtp("user", "9000000100");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> otpAuthService.verifyOtp("user", challenge.getChallengeId(), "000000"));

        assertEquals(ErrorCode.OTP_INVALID, ex.getErrorCode());
    }

    @Test
    void shouldCreateValidSessionAfterSuccessfulVerification() {
        OtpChallengeResponse challenge = otpAuthService.requestOtp("user", "9000000101");
        OtpVerifyResponse verify = otpAuthService.verifyOtp("user", challenge.getChallengeId(), challenge.getOtp());

        assertNotNull(verify.getSessionToken());
        assertTrue(otpAuthService.isValidSession("user", verify.getSessionToken()));
        assertFalse(otpAuthService.isValidSession("admin", verify.getSessionToken()));
    }

    @Test
    void shouldRejectBlankSessionToken() {
        assertFalse(otpAuthService.isValidSession("user", null));
        assertFalse(otpAuthService.isValidSession("user", ""));
        assertFalse(otpAuthService.isValidSession("user", "   "));
    }
}

