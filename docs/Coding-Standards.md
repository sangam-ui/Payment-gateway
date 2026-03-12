# Coding Standards

## 1) Design rules
- Follow layered architecture: `controller -> service -> repository`.
- Keep controller methods thin and orchestration in service layer.
- Use immutable response DTOs where practical.
- Prefer explicit method names for payment semantics.

## 2) Error handling
- Throw `BusinessException` for business rule failures only.
- Use `ErrorCode` for machine-readable error classification.
- Convert all exceptions in `GlobalExceptionHandler` to consistent JSON shape.

## 3) Generics usage
- Use `ApiResponse<T>` for all API responses.
- Avoid raw types.
- Favor type inference (`new ApiResponse<>(...)`) over explicit type repetition.

## 4) Complexity and maintainability
- Keep methods focused and short.
- Extract reusable private helpers for repeated orchestration logic.
- Prefer O(1) local operations and bounded DB access per request.

## 5) Testing
- Add/modify tests for every business behavior change.
- Validate success and failure paths (including compensation).
- Keep integration tests deterministic by clearing state in `@BeforeEach`.

## 6) Git workflow
- Branch naming: `feature/<scope>-<short-name>`.
- Commit style: `feat|fix|refactor|test|docs: message`.
- Always merge via PR into `main` after green CI.

