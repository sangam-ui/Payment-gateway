package org.example.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class BankAccountRequest {

    @NotBlank(message = "phone is required")
    private String phone;

    @NotBlank(message = "accountHolder is required")
    private String accountHolder;

    @NotBlank(message = "bankName is required")
    private String bankName;

    @NotBlank(message = "accountNumber is required")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "accountNumber must be 9-18 digits")
    private String accountNumber;

    @NotBlank(message = "ifscCode is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "ifscCode must be valid")
    private String ifscCode;

    private String upiId;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}

