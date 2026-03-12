package org.example.service;

import org.example.model.ReceiptEntity;
import org.example.repository.ReceiptRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@ConditionalOnProperty(name = "app.s3.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryReceiptStorageService implements ReceiptStorageService {

    private final ReceiptRepository receiptRepository;

    public InMemoryReceiptStorageService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Override
    public String store(String key, String content) {
        receiptRepository.save(new ReceiptEntity(key, content, Instant.now()));
        return key;
    }
}
