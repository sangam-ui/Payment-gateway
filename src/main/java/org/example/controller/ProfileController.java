package org.example.controller;

import org.example.api.ApiResponse;
import org.example.dto.BankAccountRequest;
import org.example.dto.BankAccountResponse;
import org.example.dto.KycProfileResponse;
import org.example.dto.KycRequest;
import org.example.service.CustomerProfileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@Validated
public class ProfileController {

    private final CustomerProfileService customerProfileService;

    public ProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @PostMapping("/kyc")
    public ApiResponse<KycProfileResponse> submitKyc(@Valid @RequestBody KycRequest request) {
        return ApiResponse.success("KYC verified", customerProfileService.upsertKyc(request));
    }

    @GetMapping("/{phone}/kyc")
    public ApiResponse<KycProfileResponse> getKyc(@PathVariable("phone") String phone) {
        return ApiResponse.success("KYC fetched", customerProfileService.getKyc(phone));
    }

    @PostMapping("/banks")
    public ApiResponse<BankAccountResponse> addBank(@Valid @RequestBody BankAccountRequest request) {
        return ApiResponse.success("Bank account added", customerProfileService.addBankAccount(request));
    }

    @GetMapping("/{phone}/banks")
    public ApiResponse<List<BankAccountResponse>> listBanks(@PathVariable("phone") String phone) {
        return ApiResponse.success("Bank accounts fetched", customerProfileService.getBankAccounts(phone));
    }
}

