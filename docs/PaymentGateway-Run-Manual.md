# Payment Gateway - Run Manual (Business Logic Testing)

Use this manual to run the service and validate business flows end-to-end.

## 1) Prerequisites
- Java 8+ and Maven installed
- Docker Desktop running (if you want MySQL in container)
- Postman installed

## 2) Start MySQL (optional but recommended)
If local MySQL is not running, start from `docker-compose.yml`.

```powershell
docker compose up -d
```

## 3) Start the application
Run from project root.

```powershell
mvn spring-boot:run
```

Health check:

```powershell
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}`

## 4) Authentication to use in Postman
Use HTTP Basic auth on secured APIs:
- `user / user123`
- `admin / admin123`

## 5) Import Postman collection
- File: `postman/Payment-Gateway.postman_collection.json`
- Run folders in this order:
  1. Happy Flows
  2. Validation and Auth
  3. Fallback Flows

## 5.1) Import Postman environment (recommended)
- File: `postman/Payment-Gateway.postman_environment.json`
- Select environment: `Payment Gateway - Local`
- If needed, update values:
  - `baseUrl` (default `http://localhost:8080`)
  - `username` / `password`
  - `phoneA`, `phoneB`, `phoneC`

## 6) Business Logic Validation Checklist

### A. Happy flow checks
1. `Add Money`
   - Endpoint: `POST /api/v1/wallet/add-money`
   - Expect: `success=true`, `data.status=SUCCESS`, `data.type=ADD_MONEY`
2. `Send Money`
   - Endpoint: `POST /api/v1/payments/send-money`
   - Expect: `success=true`, `data.status=SUCCESS`, `data.type=SEND_MONEY`
3. `Receive Money`
   - Endpoint: `POST /api/v1/payments/receive-money`
   - Expect: `success=true`, `data.status=SUCCESS`, `data.type=RECEIVE_MONEY`
4. `Pay Merchant` (normal merchantId)
   - Expect: `success=true`, `data.status=SUCCESS`, `data.receiptKey` not null
5. `Pay Electricity Bill` and `Recharge`
   - Expect: `success=true`, `data.status=SUCCESS`
6. `Get Transaction`
   - Endpoint: `GET /api/v1/payments/transactions/{transactionId}`
   - Expect: transaction JSON for previous operation

### B. Validation and auth checks
1. Invalid add-money payload (`phone` empty, `amount` 0)
   - Expect: HTTP `400`, `success=false`, `data.code=VALIDATION_FAILED`
2. Unauthorized balance request (no auth)
   - Expect: HTTP `401`
3. Unknown transaction id
   - Expect: HTTP `404`, `success=false`, `data.code=RESOURCE_NOT_FOUND`
4. Insufficient balance send-money
   - Expect: HTTP `400`, `success=false`, `data.code=INSUFFICIENT_BALANCE`

### C. Saga compensation checks (important)
1. Merchant fallback request (`merchantId` contains `FAIL`)
   - Expect: HTTP `200`, transaction `status=COMPENSATED`
2. Biller fallback request (`provider` contains `FAIL`)
   - Expect: HTTP `200`, transaction `status=COMPENSATED`
3. Balance check after compensation
   - Expect: sender wallet returns to pre-debit value

## 7) Optional DB verification (MySQL)
Verify that business data is persisted.

```powershell
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; show tables;"
```

```powershell
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; select phone,balance from wallets;"
```

```powershell
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; select id,type,status,amount,source_phone,target_reference from transactions order by created_at desc limit 20;"
```

```powershell
docker exec -it payment-gateway-mysql mysql -uroot -proot -e "use payment_gateway; select receipt_key,created_at from receipts order by created_at desc limit 20;"
```

## 8) Regression quick check
Run automated tests:

```powershell
mvn test
```

Expected: all tests pass and JaCoCo report generated at `target/site/jacoco/index.html`.
