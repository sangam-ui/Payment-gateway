package org.example.controller;

import org.example.api.ApiResponse;
import org.example.dto.OtpChallengeResponse;
import org.example.dto.OtpRequest;
import org.example.dto.OtpVerifyRequest;
import org.example.dto.OtpVerifyResponse;
import org.example.service.OtpAuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final OtpAuthService otpAuthService;

    public AuthController(OtpAuthService otpAuthService) {
        this.otpAuthService = otpAuthService;
    }

    @PostMapping("/otp/request")
    public ApiResponse<OtpChallengeResponse> requestOtp(@Valid @RequestBody OtpRequest request, Principal principal) {
        OtpChallengeResponse response = otpAuthService.requestOtp(principal.getName(), request.getPhone());
        return ApiResponse.success("OTP generated", response);
    }

    @PostMapping("/otp/verify")
    public ApiResponse<OtpVerifyResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request, Principal principal) {
        OtpVerifyResponse response = otpAuthService.verifyOtp(principal.getName(), request.getChallengeId(), request.getOtp());
        return ApiResponse.success("2FA verification successful", response);
    }
}

