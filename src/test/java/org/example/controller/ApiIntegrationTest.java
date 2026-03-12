package org.example.controller;

import org.example.repository.ReceiptRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @BeforeEach
    void clearData() {
        receiptRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void shouldRejectUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/balance/9000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAddMoneyAndFetchBalance() throws Exception {
        mockMvc.perform(post("/api/v1/wallet/add-money")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000001\",\"amount\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        mockMvc.perform(get("/api/v1/wallet/balance/9000000001")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    void shouldCompensateMerchantPaymentWhenProviderFails() throws Exception {
        mockMvc.perform(post("/api/v1/wallet/add-money")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000002\",\"amount\":600}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/payments/merchant")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000002\",\"merchantId\":\"FAIL-STORE\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPENSATED"));

        mockMvc.perform(get("/api/v1/wallet/balance/9000000002")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(600));
    }

    @Test
    void shouldReturnBadRequestForInvalidAddMoneyPayload() throws Exception {
        mockMvc.perform(post("/api/v1/wallet/add-money")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturnBadRequestForInsufficientBalance() throws Exception {
        mockMvc.perform(post("/api/v1/payments/send-money")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000090\",\"toPhone\":\"9000000091\",\"amount\":50}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INSUFFICIENT_BALANCE"));
    }

    @Test
    void shouldReceiveMoneyAndUpdateReceiverBalance() throws Exception {
        mockMvc.perform(post("/api/v1/wallet/add-money")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000003\",\"amount\":700}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/payments/receive-money")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000003\",\"toPhone\":\"9000000004\",\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("RECEIVE_MONEY"));

        mockMvc.perform(get("/api/v1/wallet/balance/9000000004")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(200));
    }

    @Test
    void shouldReturnBadRequestForUnknownTransaction() throws Exception {
        mockMvc.perform(get("/api/v1/payments/transactions/TXN-NOT-FOUND")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("RESOURCE_NOT_FOUND"));
    }
}
