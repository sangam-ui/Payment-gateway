package org.example.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SendMoneyRequest {

    @NotBlank(message = "fromPhone is required")
    private String fromPhone;

    @NotBlank(message = "toPhone is required")
    private String toPhone;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.0", message = "amount must be >= 1")
    private BigDecimal amount;

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getToPhone() {
        return toPhone;
    }

    public void setToPhone(String toPhone) {
        this.toPhone = toPhone;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

