# Payment Gateway - Run and Test Manual (Backend)

Use this manual for complete backend setup, run, end-to-end flow validation, and regression testing.

## 1) Prerequisites
- Java 8+ and Maven
- Docker Desktop (optional, if running MySQL via container)
- Postman (recommended) or PowerShell API calls

## 2) Start Database (MySQL runtime profile)
If local MySQL is not already available, use Docker:

```powershell
docker compose up -d
```

## 3) Start Application
Run from project root:

```powershell
mvn spring-boot:run
```

Health check:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/actuator/health"
```

Expected: `status = UP`.

## 4) Authentication and 2FA Bootstrap (Mandatory)
All business APIs under `/api/v1/**` need:
1. Basic Auth
2. `X-Session-Token` from OTP verify

Default credentials:
- `user / user123`
- `admin / admin123`

### 4.1) PowerShell setup helpers

```powershell
$BaseUrl = "http://localhost:8080"
$Username = "user"
$Password = "user123"
$PhoneA = "9000000001"
$PhoneB = "9000000002"

$BasicToken = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${Username}:${Password}"))
$AuthHeaders = @{ Authorization = "Basic $BasicToken"; "Content-Type" = "application/json" }
```

### 4.2) Request OTP

```powershell
$OtpRequestBody = @{ phone = $PhoneA } | ConvertTo-Json
$OtpRequestResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/otp/request" -Headers $AuthHeaders -Body $OtpRequestBody
$ChallengeId = $OtpRequestResp.data.challengeId
$Otp = $OtpRequestResp.data.otp
```

### 4.3) Verify OTP and get session token

```powershell
$OtpVerifyBody = @{ challengeId = $ChallengeId; otp = $Otp } | ConvertTo-Json
$OtpVerifyResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/otp/verify" -Headers $AuthHeaders -Body $OtpVerifyBody
$SessionToken = $OtpVerifyResp.data.sessionToken
$ApiHeaders = @{ Authorization = "Basic $BasicToken"; "Content-Type" = "application/json"; "X-Session-Token" = $SessionToken }
```

## 5) Complete Business Flow (Step by Step)

### Step 1: Add money

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/wallet/add-money" -Headers $ApiHeaders -Body (@{ phone = $PhoneA; amount = 5000 } | ConvertTo-Json)
```

Expect: `success=true`, `data.type=ADD_MONEY`, `data.status=SUCCESS`.

### Step 2: Send money

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/send-money" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; toPhone = $PhoneB; amount = 500 } | ConvertTo-Json)
```

Expect: `data.type=SEND_MONEY`, `data.status=SUCCESS`.

### Step 3: Receive money

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/receive-money" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; toPhone = $PhoneB; amount = 100 } | ConvertTo-Json)
```

Expect: `data.type=RECEIVE_MONEY`, `data.status=SUCCESS`.

### Step 4: Merchant payment

```powershell
$MerchantResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/merchant" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; merchantId = "SHOP-101"; amount = 250 } | ConvertTo-Json)
$TxnId = $MerchantResp.data.id
```

Expect: `data.type=MERCHANT_PAYMENT`, `data.status=SUCCESS`, `data.receiptKey` present.

### Step 5: Electricity bill

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/electricity-bill" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; provider = "TATA_POWER"; consumerNumber = "CN-001"; amount = 120 } | ConvertTo-Json)
```

Expect: `data.type=ELECTRICITY_BILL`, `data.status=SUCCESS`.

### Step 6: Recharge

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/recharge" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; operatorName = "AIRTEL"; mobileNumber = "7000000000"; amount = 99 } | ConvertTo-Json)
```

Expect: `data.type=MOBILE_RECHARGE`, `data.status=SUCCESS`.

### Step 7: KYC submit

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/profile/kyc" -Headers $ApiHeaders -Body (@{ phone = $PhoneA; fullName = "Test User"; email = "test.user@example.com"; panNumber = "ABCDE1234F" } | ConvertTo-Json)
```

Expect: `data.status=VERIFIED`.

### Step 8: Add beneficiary bank

```powershell
$BankResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/profile/banks" -Headers $ApiHeaders -Body (@{ phone = $PhoneA; accountHolder = "Test User"; bankName = "HDFC"; accountNumber = "123456789012"; ifscCode = "HDFC0123456"; upiId = "test@hdfc" } | ConvertTo-Json)
$BankId = $BankResp.data.id
```

Expect: `data.id` exists, `data.accountMasked` returned.

### Step 9: UPI transfer

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/upi-transfer" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; toUpiId = "friend@okaxis"; amount = 150 } | ConvertTo-Json)
```

Expect: `data.type=UPI_TRANSFER`, `data.status=SUCCESS`.

### Step 10: Bank transfer

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/bank-transfer" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; beneficiaryBankAccountId = $BankId; amount = 200 } | ConvertTo-Json)
```

Expect: `data.type=BANK_TRANSFER`, `data.status=SUCCESS`.

### Step 11: Transaction fetch and history

```powershell
Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/payments/transactions/$TxnId" -Headers $ApiHeaders
Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/payments/history/$PhoneA" -Headers $ApiHeaders
```

## 6) Compensation Flow Checks (Saga)

### Merchant compensation (`merchantId` contains `FAIL`)

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/merchant" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; merchantId = "FAIL-STORE"; amount = 100 } | ConvertTo-Json)
```

Expect: HTTP `200`, `data.status=COMPENSATED`.

### Biller compensation (`provider` contains `FAIL`)

```powershell
Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/payments/electricity-bill" -Headers $ApiHeaders -Body (@{ fromPhone = $PhoneA; provider = "FAIL-BILLER"; consumerNumber = "CN-FAIL"; amount = 80 } | ConvertTo-Json)
```

Expect: HTTP `200`, `data.status=COMPENSATED`.

## 7) Validation and Auth Checks
- Invalid add-money payload -> HTTP `400`, `data.code=VALIDATION_FAILED`
- No auth -> HTTP `401`
- Missing `X-Session-Token` with valid Basic auth -> HTTP `401`, `data.code=OTP_REQUIRED`
- Unknown transaction -> HTTP `404`, `data.code=RESOURCE_NOT_FOUND`
- Insufficient balance -> HTTP `400`, `data.code=INSUFFICIENT_BALANCE`
- UPI/bank transfer without KYC -> HTTP `400`, `data.code=KYC_NOT_COMPLETED`

## 8) Optional DB Verification (MySQL)

```powershell
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; show tables;"
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; select phone,balance from wallets;"
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; select id,type,status,amount,source_phone,target_reference from transactions order by created_at desc limit 20;"
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; select receipt_key,created_at from receipts order by created_at desc limit 20;"
```

## 9) Automated Regression
Run complete tests:

```powershell
mvn test
```

Run focused suites:

```powershell
mvn -Dtest=ApiIntegrationTest,SagaOrchestratorServiceTest,OtpAuthServiceTest,S3ReceiptStorageServiceTest test
```

JaCoCo report: `target/site/jacoco/index.html`.

## 10) Postman Alternative (if preferred)
- Import `postman/Payment-Gateway.postman_collection.json`
- Import `postman/Payment-Gateway.postman_environment.json`
- Run order:
  1. Happy Flows
  2. Validation and Auth
  3. Fallback Flows


