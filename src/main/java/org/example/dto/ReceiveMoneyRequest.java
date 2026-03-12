package org.example.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ReceiveMoneyRequest {

    @NotBlank(message = "toPhone is required")
    private String toPhone;

    @NotBlank(message = "fromPhone is required")
    private String fromPhone;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.0", message = "amount must be >= 1")
    private BigDecimal amount;

    public String getToPhone() {
        return toPhone;
    }

    public void setToPhone(String toPhone) {
        this.toPhone = toPhone;
    }

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

