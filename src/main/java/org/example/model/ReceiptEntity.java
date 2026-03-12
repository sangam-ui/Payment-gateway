package org.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "receipts")
public class ReceiptEntity {

    @Id
    @Column(name = "receipt_key", length = 255)
    private String key;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReceiptEntity() {
    }

    public ReceiptEntity(String key, String content, Instant createdAt) {
        this.key = key;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
