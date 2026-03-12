package org.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String sourcePhone;

    @Column(nullable = false, length = 60)
    private String targetReference;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(length = 255)
    private String receiptKey;

    @Column(nullable = false)
    private Instant createdAt;

    protected TransactionEntity() {
    }

    public TransactionEntity(String id,
                             PaymentType type,
                             TransactionStatus status,
                             BigDecimal amount,
                             String sourcePhone,
                             String targetReference,
                             String message,
                             String receiptKey,
                             Instant createdAt) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.sourcePhone = sourcePhone;
        this.targetReference = targetReference;
        this.message = message;
        this.receiptKey = receiptKey;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public PaymentType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getSourcePhone() {
        return sourcePhone;
    }

    public String getTargetReference() {
        return targetReference;
    }

    public String getMessage() {
        return message;
    }

    public String getReceiptKey() {
        return receiptKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

