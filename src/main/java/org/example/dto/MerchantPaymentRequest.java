package org.example.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MerchantPaymentRequest {

    @NotBlank(message = "fromPhone is required")
    private String fromPhone;

    @NotBlank(message = "merchantId is required")
    private String merchantId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.0", message = "amount must be >= 1")
    private BigDecimal amount;

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

