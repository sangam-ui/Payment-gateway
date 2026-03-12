package org.example.controller;

import org.example.api.ApiResponse;
import org.example.api.ErrorCode;
import org.example.dto.BillPaymentRequest;
import org.example.dto.MerchantPaymentRequest;
import org.example.dto.RechargeRequest;
import org.example.dto.ReceiveMoneyRequest;
import org.example.dto.SendMoneyRequest;
import org.example.exception.BusinessException;
import org.example.model.Transaction;
import org.example.service.SagaOrchestratorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
public class PaymentController {

    private final SagaOrchestratorService sagaOrchestratorService;

    public PaymentController(SagaOrchestratorService sagaOrchestratorService) {
        this.sagaOrchestratorService = sagaOrchestratorService;
    }

    @PostMapping("/send-money")
    public ApiResponse<Transaction> sendMoney(@Valid @RequestBody SendMoneyRequest request) {
        Transaction transaction = sagaOrchestratorService.sendMoney(request.getFromPhone(), request.getToPhone(), request.getAmount());
        return ApiResponse.success("Money sent", transaction);
    }

    @PostMapping("/receive-money")
    public ApiResponse<Transaction> receiveMoney(@Valid @RequestBody ReceiveMoneyRequest request) {
        Transaction transaction = sagaOrchestratorService.receiveMoney(request.getToPhone(), request.getFromPhone(), request.getAmount());
        return ApiResponse.success("Money received", transaction);
    }

    @PostMapping("/merchant")
    public ApiResponse<Transaction> payMerchant(@Valid @RequestBody MerchantPaymentRequest request) {
        Transaction transaction = sagaOrchestratorService.payMerchant(request.getFromPhone(), request.getMerchantId(), request.getAmount());
        return ApiResponse.success("Merchant flow completed", transaction);
    }

    @PostMapping("/electricity-bill")
    public ApiResponse<Transaction> payElectricity(@Valid @RequestBody BillPaymentRequest request) {
        Transaction transaction = sagaOrchestratorService.payElectricityBill(
                request.getFromPhone(),
                request.getProvider(),
                request.getConsumerNumber(),
                request.getAmount()
        );
        return ApiResponse.success("Bill flow completed", transaction);
    }

    @PostMapping("/recharge")
    public ApiResponse<Transaction> recharge(@Valid @RequestBody RechargeRequest request) {
        Transaction transaction = sagaOrchestratorService.rechargeMobile(
                request.getFromPhone(),
                request.getOperatorName(),
                request.getMobileNumber(),
                request.getAmount()
        );
        return ApiResponse.success("Recharge flow completed", transaction);
    }

    @GetMapping("/transactions/{transactionId}")
    public ApiResponse<Transaction> transaction(@PathVariable("transactionId") String transactionId) {
        Transaction transaction = sagaOrchestratorService.getTransaction(transactionId);
        if (transaction == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Transaction not found");
        }
        return ApiResponse.success("Transaction fetched", transaction);
    }
}
