package org.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bank_accounts")
public class BankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 80)
    private String accountHolder;

    @Column(nullable = false, length = 80)
    private String bankName;

    @Column(nullable = false, length = 8)
    private String accountLast4;

    @Column(nullable = false, length = 20)
    private String ifscCode;

    @Column(length = 100)
    private String upiId;

    protected BankAccountEntity() {
    }

    public BankAccountEntity(String phone, String accountHolder, String bankName, String accountLast4, String ifscCode, String upiId) {
        this.phone = phone;
        this.accountHolder = accountHolder;
        this.bankName = bankName;
        this.accountLast4 = accountLast4;
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

    public String getAccountLast4() {
        return accountLast4;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getUpiId() {
        return upiId;
    }
}

