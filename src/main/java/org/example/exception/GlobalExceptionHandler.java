package org.example.exception;

import org.example.api.ApiError;
import org.example.api.ApiResponse;
import org.example.api.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ApiError payload = new ApiError(
                Instant.now(),
                status.value(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(ApiResponse.failure("Business validation failed", payload));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ApiError payload = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_FAILED.name(),
                errors,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(ApiResponse.failure("Request validation failed", payload));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiError>> handleGeneric(Exception ex, HttpServletRequest request) {
        ApiError payload = new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_ERROR.name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Internal server error", payload));
    }
}
