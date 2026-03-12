# Payment Gateway (Java 8, Spring Boot)

This implementation provides a 3-service style payment gateway with:
- Saga orchestration for payment flows
- Circuit breaker fallback for external calls
- Spring Security basic auth
- Global exception handling with error codes
- Generic API response model (`ApiResponse<T>`)
- MySQL persistence for wallets, transactions, and receipts
- Optional S3 receipt upload

Architecture and planning docs:
- `docs/PaymentGateway-HLD-LLD.md`
- `docs/Phase-Wise-Roadmap.md`
- `docs/Coding-Standards.md`
- `docs/Complexity-Notes.md`
- `docs/PaymentGateway-Run-Manual.md`

## Runtime DB (MySQL)
Default runtime config in `src/main/resources/application.yml`:
- url: `jdbc:mysql://localhost:3306/payment_gateway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- username: `root`
- password: `root`

Quick local MySQL (optional, Docker):
```powershell
docker compose up -d
```

Update username/password based on your local MySQL setup.

## Run
```powershell
mvn spring-boot:run
```

## Test + JaCoCo
Tests run with H2 test profile (`src/test/resources/application-test.yml`), so MySQL is not required for unit/integration test execution.

```powershell
mvn test
```

JaCoCo HTML report:
- `target/site/jacoco/index.html`

## Auth (Basic)
- `user:user123`
- `admin:admin123`

## API test coverage
- Unit/service tests: `src/test/java/org/example/service/SagaOrchestratorServiceTest.java`
- Integration tests (MockMvc + Basic auth): `src/test/java/org/example/controller/ApiIntegrationTest.java`

## Postman collection
- File: `postman/Payment-Gateway.postman_collection.json`
- Environment: `postman/Payment-Gateway.postman_environment.json`
- Folders included:
  - Happy Flows
  - Fallback Flows (compensation)
  - Validation and Auth

Import the collection and environment, then run requests in order for best flow verification.

## Quick Postman Flows
1. Add money
   - `POST /api/v1/wallet/add-money`
   - body: `{ "phone": "9999999999", "amount": 5000 }`
2. Send money
   - `POST /api/v1/payments/send-money`
3. Receive money
   - `POST /api/v1/payments/receive-money`
4. Merchant payment
   - `POST /api/v1/payments/merchant`
5. Electricity bill
   - `POST /api/v1/payments/electricity-bill`
6. Recharge
   - `POST /api/v1/payments/recharge`

Use `merchantId` or `provider` containing `FAIL` to validate fallback + Saga compensation.

## Branch workflow (industry)
1. Create feature branch from main
```powershell
git checkout main
git pull origin main
git checkout -b feature/<scope>-<name>
```
2. Commit and push
```powershell
git add .
git commit -m "feat: <change summary>"
git push -u origin feature/<scope>-<name>
```
3. Open PR to `main` and merge only after CI green.

## CI quality gate
- GitHub Actions workflow: `.github/workflows/ci.yml`
- Enforced in build:
  - JaCoCo LINE >= 0.65
  - JaCoCo BRANCH >= 0.30
