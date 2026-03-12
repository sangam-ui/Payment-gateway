# Payment Gateway - Single Page Analysis, HLD, and LLD

## 1) Problem Statement and Scope
A wallet-first payment gateway where users can:
- add money
- send money to phone number (Paytm-style)
- receive money
- pay merchants
- pay electricity bills
- recharge mobile numbers

Design stack and constraints:
- Java 8 + Spring Boot REST
- Spring Security (basic auth)
- Saga orchestration + Circuit Breaker resilience
- generic API responses + global exception handling
- MySQL for persistent storage (wallets, transactions, receipts)
- optional S3 receipt storage when enabled
- Postman-first validation

## 2) High-Level Design (3 Logical Microservices)
1. **API-Orchestrator Service**
   - Entry point for all REST APIs (`/api/v1/**`)
   - Auth, validation, transaction orchestration, compensation decisions
   - Main class: `SagaOrchestratorService`
2. **Wallet Service**
   - Wallet debit/credit/top-up/balance operations
   - Stores balances in MySQL table `wallets`
   - Main class: `WalletService`
3. **Connector Service**
   - External operations (merchant, biller, recharge)
   - Wrapped with circuit breakers + fallback
   - Main classes: `MerchantService`, `BillerService`

## 3) Core Patterns
### Saga (orchestration-based)
For merchant/bill/recharge flows:
1) debit wallet
2) call connector
3) if connector fails -> compensate by crediting wallet
4) persist transaction with `SUCCESS` or `COMPENSATED`

### Circuit Breaker (Resilience4j)
- `merchantService` breaker for merchant calls
- `billerService` breaker for bill/recharge calls
- fallback/exception path is converted to controlled compensation outcome

## 4) LLD Components
- **Controller layer**
  - `WalletController`
  - `PaymentController`
- **DTO layer**
  - `AddMoneyRequest`, `SendMoneyRequest`, `ReceiveMoneyRequest`
  - `MerchantPaymentRequest`, `BillPaymentRequest`, `RechargeRequest`
- **Domain layer**
  - `Transaction`, `PaymentType`, `TransactionStatus`
- **Persistence layer (MySQL)**
  - Entities: `WalletEntity`, `TransactionEntity`, `ReceiptEntity`
  - Repositories: `WalletRepository`, `TransactionRepository`, `ReceiptRepository`
- **Cross-cutting**
  - Generic response: `ApiResponse<T>`
  - Global errors: `GlobalExceptionHandler`
  - Security: `SecurityConfig`

## 5) DB Connectivity and Tables
Default runtime datasource (`application.yml`):
- URL: `jdbc:mysql://localhost:3306/payment_gateway`
- Username: `root`
- Password: `root`
- Driver: `com.mysql.cj.jdbc.Driver`
- JPA DDL: `update`

Key tables:
- `wallets` (`phone` PK, `balance`, `version`)
- `transactions` (`id` PK, `type`, `status`, `amount`, `source_phone`, `target_reference`, `message`, `receipt_key`, `created_at`)
- `receipts` (`key` PK, `content`, `created_at`)

## 6) REST API Contract (Postman)
- `POST /api/v1/wallet/add-money`
- `GET /api/v1/wallet/balance/{phone}`
- `POST /api/v1/payments/send-money`
- `POST /api/v1/payments/receive-money`
- `POST /api/v1/payments/merchant`
- `POST /api/v1/payments/electricity-bill`
- `POST /api/v1/payments/recharge`
- `GET /api/v1/payments/transactions/{transactionId}`

Auth users for Postman:
- `user / user123`
- `admin / admin123`

## 7) Non-Functional and Quality
- clear package layering for maintainability
- MySQL persistence in runtime profile
- H2 test profile for local/CI tests without MySQL
- JaCoCo report via Maven test lifecycle (`target/site/jacoco/index.html`)
- integration + unit tests for auth, validation, success, fallback, compensation

## 8) Scale-Out Path (Production Direction)
- split logical services into separate deployables
- move from basic auth to JWT/OAuth2
- add durable event/outbox for production-grade Saga reliability
