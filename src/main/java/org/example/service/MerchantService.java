package org.example.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MerchantService {

    @CircuitBreaker(name = "merchantService", fallbackMethod = "fallbackPayMerchant")
    public ExternalOperationResult payMerchant(String merchantId, BigDecimal amount) {
        if (merchantId.toUpperCase().contains("FAIL")) {
            throw new IllegalStateException("Merchant provider unavailable");
        }
        return new ExternalOperationResult(true, "M-" + UUID.randomUUID().toString(), "Merchant payment accepted");
    }

    public ExternalOperationResult fallbackPayMerchant(String merchantId, BigDecimal amount, Throwable throwable) {
        return new ExternalOperationResult(false, null, "Merchant service fallback: " + throwable.getMessage());
    }
}

