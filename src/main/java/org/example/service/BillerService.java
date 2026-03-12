package org.example.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BillerService {

    @CircuitBreaker(name = "billerService", fallbackMethod = "fallbackPayBill")
    public ExternalOperationResult payBill(String provider, String consumerNumber, BigDecimal amount) {
        if (provider.toUpperCase().contains("FAIL")) {
            throw new IllegalStateException("Biller service unavailable");
        }
        return new ExternalOperationResult(true, "B-" + UUID.randomUUID().toString(), "Bill paid successfully");
    }

    @CircuitBreaker(name = "billerService", fallbackMethod = "fallbackRecharge")
    public ExternalOperationResult recharge(String operatorName, String mobileNumber, BigDecimal amount) {
        if (operatorName.toUpperCase().contains("FAIL")) {
            throw new IllegalStateException("Recharge service unavailable");
        }
        return new ExternalOperationResult(true, "R-" + UUID.randomUUID().toString(), "Recharge successful");
    }

    public ExternalOperationResult fallbackPayBill(String provider, String consumerNumber, BigDecimal amount, Throwable throwable) {
        return new ExternalOperationResult(false, null, "Biller fallback: " + throwable.getMessage());
    }

    public ExternalOperationResult fallbackRecharge(String operatorName, String mobileNumber, BigDecimal amount, Throwable throwable) {
        return new ExternalOperationResult(false, null, "Recharge fallback: " + throwable.getMessage());
    }
}

