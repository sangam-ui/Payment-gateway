package org.example.controller;

import org.example.api.ApiResponse;
import org.example.dto.AddMoneyRequest;
import org.example.model.Transaction;
import org.example.service.SagaOrchestratorService;
import org.example.service.WalletService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/wallet")
@Validated
public class WalletController {

    private final SagaOrchestratorService sagaOrchestratorService;
    private final WalletService walletService;

    public WalletController(SagaOrchestratorService sagaOrchestratorService, WalletService walletService) {
        this.sagaOrchestratorService = sagaOrchestratorService;
        this.walletService = walletService;
    }

    @PostMapping("/add-money")
    public ApiResponse<Transaction> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        Transaction transaction = sagaOrchestratorService.addMoney(request.getPhone(), request.getAmount());
        return ApiResponse.success("Money added", transaction);
    }

    @GetMapping("/balance/{phone}")
    public ApiResponse<BigDecimal> balance(@PathVariable("phone") String phone) {
        return ApiResponse.success("Balance fetched", walletService.getBalance(phone));
    }
}

