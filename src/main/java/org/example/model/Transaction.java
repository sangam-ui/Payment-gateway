package org.example.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {

    private final String id;
    private final PaymentType type;
    private final TransactionStatus status;
    private final BigDecimal amount;
    private final String sourcePhone;
    private final String targetReference;
    private final String message;
    private final String receiptKey;
    private final Instant createdAt;

    public Transaction(String id,
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

