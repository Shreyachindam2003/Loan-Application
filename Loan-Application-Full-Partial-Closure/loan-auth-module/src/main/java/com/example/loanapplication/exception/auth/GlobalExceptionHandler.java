package com.example.loanapplication.exception.auth;

import com.example.loanapplication.enums.auth.AuthErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", ex.getErrorCode().name());
        body.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", getErrorCode(ex.getStatusCode().value()).name());
        body.put("message", ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", AuthErrorCode.VALIDATION_ERROR.name());
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private AuthErrorCode getErrorCode(int status) {
        return switch (status) {
            case 400 -> AuthErrorCode.BAD_REQUEST;
            case 401 -> AuthErrorCode.UNAUTHORIZED;
            case 404 -> AuthErrorCode.NOT_FOUND;
            case 409 -> AuthErrorCode.CONFLICT;
            default -> AuthErrorCode.AUTH_ERROR;
        };
    }
}
