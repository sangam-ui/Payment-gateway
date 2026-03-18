package org.example.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BankTransferRequest {

    @NotBlank(message = "fromPhone is required")
    private String fromPhone;

    @NotNull(message = "beneficiaryBankAccountId is required")
    private Long beneficiaryBankAccountId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.0", message = "amount must be >= 1")
    private BigDecimal amount;

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public Long getBeneficiaryBankAccountId() {
        return beneficiaryBankAccountId;
    }

    public void setBeneficiaryBankAccountId(Long beneficiaryBankAccountId) {
        this.beneficiaryBankAccountId = beneficiaryBankAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

