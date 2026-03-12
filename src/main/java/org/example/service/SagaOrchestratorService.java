package org.example.service;

import org.example.model.PaymentType;
import org.example.model.Transaction;
import org.example.model.TransactionEntity;
import org.example.model.TransactionStatus;
import org.example.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class SagaOrchestratorService {

    private final WalletService walletService;
    private final MerchantService merchantService;
    private final BillerService billerService;
    private final ReceiptStorageService receiptStorageService;
    private final TransactionRepository transactionRepository;

    public SagaOrchestratorService(WalletService walletService,
                                   MerchantService merchantService,
                                   BillerService billerService,
                                   ReceiptStorageService receiptStorageService,
                                   TransactionRepository transactionRepository) {
        this.walletService = walletService;
        this.merchantService = merchantService;
        this.billerService = billerService;
        this.receiptStorageService = receiptStorageService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction addMoney(String phone, BigDecimal amount) {
        walletService.addMoney(phone, amount);
        return persist(PaymentType.ADD_MONEY, TransactionStatus.SUCCESS, amount, phone, phone, "Wallet top-up successful", null);
    }

    @Transactional
    public Transaction sendMoney(String fromPhone, String toPhone, BigDecimal amount) {
        walletService.debit(fromPhone, amount);
        walletService.credit(toPhone, amount);
        return persist(PaymentType.SEND_MONEY, TransactionStatus.SUCCESS, amount, fromPhone, toPhone, "Money transfer successful", null);
    }

    @Transactional
    public Transaction receiveMoney(String toPhone, String fromPhone, BigDecimal amount) {
        walletService.debit(fromPhone, amount);
        walletService.credit(toPhone, amount);
        return persist(PaymentType.RECEIVE_MONEY, TransactionStatus.SUCCESS, amount, fromPhone, toPhone, "Money received successfully", null);
    }

    @Transactional
    public Transaction payMerchant(String fromPhone, String merchantId, BigDecimal amount) {
        walletService.debit(fromPhone, amount);
        ExternalOperationResult operation;
        try {
            operation = merchantService.payMerchant(merchantId, amount);
        } catch (Exception ex) {
            operation = new ExternalOperationResult(false, null, "Merchant service failed: " + ex.getMessage());
        }
        if (!operation.isSuccess()) {
            walletService.credit(fromPhone, amount);
            return persist(PaymentType.MERCHANT_PAYMENT, TransactionStatus.COMPENSATED, amount, fromPhone, merchantId, operation.getMessage(), null);
        }
        String receiptKey = createReceiptKey(fromPhone);
        receiptStorageService.store(receiptKey, buildReceiptContent(fromPhone, amount, operation.getReference()));
        return persist(PaymentType.MERCHANT_PAYMENT, TransactionStatus.SUCCESS, amount, fromPhone, merchantId, operation.getMessage(), receiptKey);
    }

    @Transactional
    public Transaction payElectricityBill(String fromPhone, String provider, String consumerNumber, BigDecimal amount) {
        walletService.debit(fromPhone, amount);
        ExternalOperationResult operation;
        try {
            operation = billerService.payBill(provider, consumerNumber, amount);
        } catch (Exception ex) {
            operation = new ExternalOperationResult(false, null, "Biller service failed: " + ex.getMessage());
        }
        if (!operation.isSuccess()) {
            walletService.credit(fromPhone, amount);
            return persist(PaymentType.ELECTRICITY_BILL, TransactionStatus.COMPENSATED, amount, fromPhone, consumerNumber, operation.getMessage(), null);
        }
        String receiptKey = createReceiptKey(fromPhone);
        receiptStorageService.store(receiptKey, buildReceiptContent(fromPhone, amount, operation.getReference()));
        return persist(PaymentType.ELECTRICITY_BILL, TransactionStatus.SUCCESS, amount, fromPhone, consumerNumber, operation.getMessage(), receiptKey);
    }

    @Transactional
    public Transaction rechargeMobile(String fromPhone, String operatorName, String mobileNumber, BigDecimal amount) {
        walletService.debit(fromPhone, amount);
        ExternalOperationResult operation;
        try {
            operation = billerService.recharge(operatorName, mobileNumber, amount);
        } catch (Exception ex) {
            operation = new ExternalOperationResult(false, null, "Recharge service failed: " + ex.getMessage());
        }
        if (!operation.isSuccess()) {
            walletService.credit(fromPhone, amount);
            return persist(PaymentType.MOBILE_RECHARGE, TransactionStatus.COMPENSATED, amount, fromPhone, mobileNumber, operation.getMessage(), null);
        }
        String receiptKey = createReceiptKey(fromPhone);
        receiptStorageService.store(receiptKey, buildReceiptContent(fromPhone, amount, operation.getReference()));
        return persist(PaymentType.MOBILE_RECHARGE, TransactionStatus.SUCCESS, amount, fromPhone, mobileNumber, operation.getMessage(), receiptKey);
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .map(this::toDomain)
                .orElse(null);
    }

    private Transaction persist(PaymentType paymentType,
                                TransactionStatus status,
                                BigDecimal amount,
                                String sourcePhone,
                                String targetReference,
                                String message,
                                String receiptKey) {
        String id = "TXN-" + UUID.randomUUID().toString();
        TransactionEntity entity = new TransactionEntity(
                id,
                paymentType,
                status,
                amount,
                sourcePhone,
                targetReference,
                message,
                receiptKey,
                Instant.now()
        );
        return toDomain(transactionRepository.save(entity));
    }

    private String createReceiptKey(String sourcePhone) {
        return "receipt/" + sourcePhone + "/" + UUID.randomUUID().toString() + ".json";
    }

    private String buildReceiptContent(String fromPhone, BigDecimal amount, String reference) {
        return "{\"fromPhone\":\"" + fromPhone + "\",\"amount\":" + amount + ",\"providerReference\":\"" + reference + "\"}";
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getType(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getSourcePhone(),
                entity.getTargetReference(),
                entity.getMessage(),
                entity.getReceiptKey(),
                entity.getCreatedAt()
        );
    }
}
