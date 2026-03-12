package org.example.service;

import org.example.model.PaymentType;
import org.example.model.Transaction;
import org.example.model.TransactionStatus;
import org.example.repository.ReceiptRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class SagaOrchestratorServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private SagaOrchestratorService sagaOrchestratorService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @BeforeEach
    void setup() {
        receiptRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void shouldAddMoney() {
        Transaction tx = sagaOrchestratorService.addMoney("9000000001", new BigDecimal("1000"));
        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(new BigDecimal("1000.00"), walletService.getBalance("9000000001"));
    }

    @Test
    void shouldSendMoneyBetweenWallets() {
        sagaOrchestratorService.addMoney("9000000001", new BigDecimal("1000"));
        Transaction tx = sagaOrchestratorService.sendMoney("9000000001", "9000000002", new BigDecimal("250"));

        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(new BigDecimal("750.00"), walletService.getBalance("9000000001"));
        assertEquals(new BigDecimal("250.00"), walletService.getBalance("9000000002"));
    }

    @Test
    void shouldCompensateWhenMerchantFails() {
        sagaOrchestratorService.addMoney("9000000001", new BigDecimal("500"));
        Transaction tx = sagaOrchestratorService.payMerchant("9000000001", "FAIL-MERCHANT", new BigDecimal("100"));

        assertEquals(TransactionStatus.COMPENSATED, tx.getStatus());
        assertEquals(new BigDecimal("500.00"), walletService.getBalance("9000000001"));
    }

    @Test
    void shouldStoreReceiptForSuccess() {
        sagaOrchestratorService.addMoney("9000000001", new BigDecimal("500"));
        Transaction tx = sagaOrchestratorService.payMerchant("9000000001", "shop-101", new BigDecimal("100"));

        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertNotNull(tx.getReceiptKey());
    }

    @Test
    void shouldReceiveMoney() {
        sagaOrchestratorService.addMoney("9000000005", new BigDecimal("800"));
        Transaction tx = sagaOrchestratorService.receiveMoney("9000000006", "9000000005", new BigDecimal("300"));

        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(PaymentType.RECEIVE_MONEY, tx.getType());
        assertEquals(new BigDecimal("500.00"), walletService.getBalance("9000000005"));
        assertEquals(new BigDecimal("300.00"), walletService.getBalance("9000000006"));
    }
}
