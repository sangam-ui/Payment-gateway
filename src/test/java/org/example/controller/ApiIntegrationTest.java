package org.example.controller;

import org.example.repository.ReceiptRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.WalletRepository;
import org.example.repository.BankAccountRepository;
import org.example.repository.KycProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final String TWO_FACTOR_HEADER = "X-Session-Token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private KycProfileRepository kycProfileRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String sessionToken;

    @BeforeEach
    void clearData() {
        bankAccountRepository.deleteAll();
        kycProfileRepository.deleteAll();
        receiptRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        sessionToken = authenticateWithTwoFactor("9000000000");
    }

    @Test
    void shouldRejectUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/balance/9000000001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWhenTwoFactorTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/wallet/balance/9000000001")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.code").value("OTP_REQUIRED"));
    }

    @Test
    void shouldAddMoneyAndFetchBalance() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000001\",\"amount\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        mockMvc.perform(authedGet("/api/v1/wallet/balance/9000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    void shouldCompensateMerchantPaymentWhenProviderFails() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000002\",\"amount\":600}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/payments/merchant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000002\",\"merchantId\":\"FAIL-STORE\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPENSATED"));

        mockMvc.perform(authedGet("/api/v1/wallet/balance/9000000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(600));
    }

    @Test
    void shouldPayElectricityBillSuccessfully() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000020\",\"amount\":1000}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/payments/electricity-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000020\",\"provider\":\"TATA_POWER\",\"consumerNumber\":\"CN-1001\",\"amount\":120}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("ELECTRICITY_BILL"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void shouldRechargeMobileSuccessfully() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000021\",\"amount\":1000}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/payments/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000021\",\"operatorName\":\"AIRTEL\",\"mobileNumber\":\"7000000001\",\"amount\":99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("MOBILE_RECHARGE"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void shouldReturnBadRequestForInvalidAddMoneyPayload() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturnBadRequestForInsufficientBalance() throws Exception {
        mockMvc.perform(authedPost("/api/v1/payments/send-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000090\",\"toPhone\":\"9000000091\",\"amount\":50}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("INSUFFICIENT_BALANCE"));
    }

    @Test
    void shouldReceiveMoneyAndUpdateReceiverBalance() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000003\",\"amount\":700}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/payments/receive-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000003\",\"toPhone\":\"9000000004\",\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("RECEIVE_MONEY"));

        mockMvc.perform(authedGet("/api/v1/wallet/balance/9000000004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(200));
    }

    @Test
    void shouldReturnBadRequestForUnknownTransaction() throws Exception {
        mockMvc.perform(authedGet("/api/v1/payments/transactions/TXN-NOT-FOUND"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldFetchExistingTransactionById() throws Exception {
        MvcResult addMoneyResult = mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000022\",\"amount\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        JsonNode transaction = objectMapper.readTree(addMoneyResult.getResponse().getContentAsString());
        String transactionId = transaction.path("data").path("id").asText();

        mockMvc.perform(authedGet("/api/v1/payments/transactions/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(transactionId))
                .andExpect(jsonPath("$.data.type").value("ADD_MONEY"));
    }

    @Test
    void shouldAllowKycThenAddBankAndTransferToUpi() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000010\",\"amount\":1000}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/profile/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000010\",\"fullName\":\"Sangam Thakur\",\"email\":\"sangam@example.com\",\"panNumber\":\"ABCDE1234F\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));

        mockMvc.perform(authedPost("/api/v1/profile/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000010\",\"accountHolder\":\"Sangam Thakur\",\"bankName\":\"HDFC Bank\",\"accountNumber\":\"123456789012\",\"ifscCode\":\"HDFC0123456\",\"upiId\":\"sangam@hdfc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountMasked").value("XXXX9012"));

        mockMvc.perform(authedPost("/api/v1/payments/upi-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000010\",\"toUpiId\":\"friend@okaxis\",\"amount\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("UPI_TRANSFER"));
    }

    @Test
    void shouldGetKycProfileForPhone() throws Exception {
        mockMvc.perform(authedPost("/api/v1/profile/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000023\",\"fullName\":\"Test User\",\"email\":\"test.user@example.com\",\"panNumber\":\"ABCDE1234F\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedGet("/api/v1/profile/9000000023/kyc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phone").value("9000000023"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));
    }

    @Test
    void shouldListBanksForPhone() throws Exception {
        mockMvc.perform(authedPost("/api/v1/profile/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000024\",\"fullName\":\"Bank User\",\"email\":\"bank.user@example.com\",\"panNumber\":\"PQRSX6789Z\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/profile/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000024\",\"accountHolder\":\"Bank User\",\"bankName\":\"ICICI\",\"accountNumber\":\"123456789123\",\"ifscCode\":\"ICIC0001234\",\"upiId\":\"bank.user@icici\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedGet("/api/v1/profile/9000000024/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].bankName").value("ICICI"))
                .andExpect(jsonPath("$.data[0].accountMasked").value("XXXX9123"));
    }

    @Test
    void shouldRejectTransferWhenKycMissing() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000011\",\"amount\":1000}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/payments/upi-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000011\",\"toUpiId\":\"friend@okhdfcbank\",\"amount\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("KYC_NOT_COMPLETED"));
    }

    @Test
    void shouldTransferToRegisteredBankAndReturnHistory() throws Exception {
        mockMvc.perform(authedPost("/api/v1/wallet/add-money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000012\",\"amount\":1200}"))
                .andExpect(status().isOk());

        mockMvc.perform(authedPost("/api/v1/profile/kyc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"9000000012\",\"fullName\":\"Rahul Sharma\",\"email\":\"rahul@example.com\",\"panNumber\":\"PQRSX6789Z\"}"))
                .andExpect(status().isOk());

        String bankPayload = "{\"phone\":\"9000000012\",\"accountHolder\":\"Rahul Sharma\",\"bankName\":\"SBI\",\"accountNumber\":\"123456789123\",\"ifscCode\":\"SBIN0123456\",\"upiId\":\"rahul@sbi\"}";

        MvcResult bankResult = mockMvc.perform(authedPost("/api/v1/profile/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bankPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        JsonNode bankJson = objectMapper.readTree(bankResult.getResponse().getContentAsString());
        long bankId = bankJson.path("data").path("id").asLong();

        mockMvc.perform(authedPost("/api/v1/payments/bank-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromPhone\":\"9000000012\",\"beneficiaryBankAccountId\":" + bankId + ",\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("BANK_TRANSFER"));

        mockMvc.perform(authedGet("/api/v1/payments/history/9000000012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("BANK_TRANSFER"));
    }

    private MockHttpServletRequestBuilder authedPost(String path) {
        return post(path)
                .with(httpBasic("user", "user123"))
                .header(TWO_FACTOR_HEADER, sessionToken);
    }

    private MockHttpServletRequestBuilder authedGet(String path) {
        return get(path)
                .with(httpBasic("user", "user123"))
                .header(TWO_FACTOR_HEADER, sessionToken);
    }

    private String authenticateWithTwoFactor(String phone) {
        try {
            MvcResult otpResult = mockMvc.perform(post("/api/v1/auth/otp/request")
                            .with(httpBasic("user", "user123"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"phone\":\"" + phone + "\"}"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode otpJson = objectMapper.readTree(otpResult.getResponse().getContentAsString());
            String challengeId = otpJson.path("data").path("challengeId").asText();
            String otp = otpJson.path("data").path("otp").asText();

            MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/otp/verify")
                            .with(httpBasic("user", "user123"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"challengeId\":\"" + challengeId + "\",\"otp\":\"" + otp + "\"}"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode verifyJson = objectMapper.readTree(verifyResult.getResponse().getContentAsString());
            return verifyJson.path("data").path("sessionToken").asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to setup authenticated 2FA session for test", ex);
        }
    }
}
