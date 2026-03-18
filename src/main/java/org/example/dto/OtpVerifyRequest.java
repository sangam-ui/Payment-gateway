package org.example.dto;

import javax.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank(message = "challengeId is required")
    private String challengeId;

    @NotBlank(message = "otp is required")
    private String otp;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

