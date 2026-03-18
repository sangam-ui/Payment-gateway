package org.example.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

public class UpiTransferRequest {

    @NotBlank(message = "fromPhone is required")
    private String fromPhone;

    @NotBlank(message = "toUpiId is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{2,}@[a-zA-Z]{2,}$", message = "toUpiId must be valid")
    private String toUpiId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.0", message = "amount must be >= 1")
    private BigDecimal amount;

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getToUpiId() {
        return toUpiId;
    }

    public void setToUpiId(String toUpiId) {
        this.toUpiId = toUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

