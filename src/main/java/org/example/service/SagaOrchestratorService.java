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
import java.util.function.Supplier;

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
        return processExternalFlow(
                PaymentType.MERCHANT_PAYMENT,
                fromPhone,
                merchantId,
                amount,
                () -> merchantService.payMerchant(merchantId, amount),
                "Merchant service failed"
        );
    }

    @Transactional
    public Transaction payElectricityBill(String fromPhone, String provider, String consumerNumber, BigDecimal amount) {
        return processExternalFlow(
                PaymentType.ELECTRICITY_BILL,
                fromPhone,
                consumerNumber,
                amount,
                () -> billerService.payBill(provider, consumerNumber, amount),
                "Biller service failed"
        );
    }

    @Transactional
    public Transaction rechargeMobile(String fromPhone, String operatorName, String mobileNumber, BigDecimal amount) {
        return processExternalFlow(
                PaymentType.MOBILE_RECHARGE,
                fromPhone,
                mobileNumber,
                amount,
                () -> billerService.recharge(operatorName, mobileNumber, amount),
                "Recharge service failed"
        );
    }

    @Transactional(readOnly = true)
    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .map(this::toDomain)
                .orElse(null);
    }

    private Transaction processExternalFlow(PaymentType paymentType,
                                            String fromPhone,
                                            String targetReference,
                                            BigDecimal amount,
                                            Supplier<ExternalOperationResult> externalCall,
                                            String failurePrefix) {
        walletService.debit(fromPhone, amount);

        ExternalOperationResult operation = safelyInvoke(externalCall, failurePrefix);
        if (!operation.isSuccess()) {
            walletService.credit(fromPhone, amount);
            return persist(paymentType, TransactionStatus.COMPENSATED, amount, fromPhone, targetReference, operation.getMessage(), null);
        }

        String receiptKey = createReceiptKey(fromPhone);
        receiptStorageService.store(receiptKey, buildReceiptContent(fromPhone, amount, operation.getReference()));
        return persist(paymentType, TransactionStatus.SUCCESS, amount, fromPhone, targetReference, operation.getMessage(), receiptKey);
    }

    private ExternalOperationResult safelyInvoke(Supplier<ExternalOperationResult> externalCall, String failurePrefix) {
        try {
            return externalCall.get();
        } catch (Exception ex) {
            return new ExternalOperationResult(false, null, failurePrefix + ": " + ex.getMessage());
        }
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
