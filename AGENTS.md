# AGENTS.md
## Scope and Goal
- This repo is a Spring Boot payment gateway (Java 8) with wallet flows, KYC/bank onboarding, and external payment connectors.
- Primary backend code is in `src/main/java/org/example`.
## Architecture You Need First
- Treat this as 3 logical services in one deployable: API orchestration, wallet ledger, and external connectors (`docs/PaymentGateway-HLD-LLD.md`).
- Controllers are thin adapters in `controller/`; most business behavior lives in `service/`.
- `SagaOrchestratorService` is the central flow engine for add/send/receive/merchant/bill/recharge/UPI/bank operations.
- Persistence is JPA repositories + entities (`repository/`, `model/`) backed by MySQL at runtime and H2 in tests.
- API response envelope is always `ApiResponse<T>` (`api/ApiResponse.java`), including failures via `GlobalExceptionHandler`.
## Security and Request Flow (Critical)
- All endpoints require Basic auth except `/actuator/health` (`config/SecurityConfig.java`).
- Business APIs under `/api/v1/**` also require `X-Session-Token` from OTP verification (`config/TwoFactorSessionFilter.java`).
- OTP flow is mandatory before protected requests:
  1) `POST /api/v1/auth/otp/request`
  2) `POST /api/v1/auth/otp/verify`
  3) send `X-Session-Token` header.
- Default users are in-memory: `user/user123`, `admin/admin123`.
## Project-Specific Business Patterns
- External flows use saga compensation in `SagaOrchestratorService.processExternalFlow(...)`: debit -> external call -> compensate (credit) on failure.
- Compensation is represented as `TransactionStatus.COMPENSATED` (HTTP 200 for these cases, not 5xx).
- Trigger connector failure intentionally using `FAIL` markers:
  - `merchantId` containing `FAIL` in `MerchantService.payMerchant(...)`
  - `provider`/`operatorName` containing `FAIL` in `BillerService`.
- UPI and bank transfer require verified KYC via `CustomerProfileService.ensureKycVerified(...)`.
## Integrations and Runtime Toggles
- MySQL runtime config is in `src/main/resources/application.yml`; Docker helper is `docker-compose.yml` (MySQL 8.4, root/root).
- Receipt storage is conditional:
  - `app.s3.enabled=false` -> `InMemoryReceiptStorageService` (actually persists to `ReceiptRepository`)
  - `app.s3.enabled=true` -> `S3ReceiptStorageService` + `AwsConfig` (region `AP_SOUTH_1`).
- Circuit breakers are Resilience4j instances `merchantService` and `billerService` configured in `application.yml`.
## Build, Test, and Debug Workflow
- Local run: `mvn spring-boot:run` (see `README.md` and `docs/PaymentGateway-Run-Manual.md`).
- Tests use H2 test profile (`src/test/resources/application-test.yml`), so MySQL is not required for `mvn test`.
- Coverage gate is enforced by Maven JaCoCo (`pom.xml`): LINE >= 0.65 and BRANCH >= 0.30.
- High-signal test references:
  - `src/test/java/org/example/controller/ApiIntegrationTest.java` (MockMvc + Basic auth + OTP bootstrap helper)
  - `src/test/java/org/example/service/SagaOrchestratorServiceTest.java` (compensation and receipt behavior).
## Conventions to Follow in Changes
- Preserve layering: `controller -> service -> repository` (`docs/Coding-Standards.md`).
- Throw `BusinessException` with `ErrorCode` for business violations; let `GlobalExceptionHandler` map response payloads.
- Keep controller responses wrapped in `ApiResponse.success(...)` and use existing error envelope shape.
- When changing business behavior, update both success and failure/compensation tests.
- Keep branch/commit style aligned with docs: `feature/<scope>-<name>`, and conventional commit prefixes.


