package org.example.dto;

public class BankAccountResponse {

    private final Long id;
    private final String phone;
    private final String accountHolder;
    private final String bankName;
    private final String accountMasked;
    private final String ifscCode;
    private final String upiId;

    public BankAccountResponse(Long id,
                               String phone,
                               String accountHolder,
                               String bankName,
                               String accountMasked,
                               String ifscCode,
                               String upiId) {
        this.id = id;
        this.phone = phone;
        this.accountHolder = accountHolder;
        this.bankName = bankName;
        this.accountMasked = accountMasked;
        this.ifscCode = ifscCode;
        this.upiId = upiId;
    }

    public Long getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountMasked() {
        return accountMasked;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getUpiId() {
        return upiId;
    }
}

