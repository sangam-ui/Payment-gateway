package org.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class WalletEntity {

    @Id
    private String phone;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;

    protected WalletEntity() {
    }

    public WalletEntity(String phone, BigDecimal balance) {
        this.phone = phone;
        this.balance = balance;
    }

    public String getPhone() {
        return phone;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

