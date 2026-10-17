
package com.example.Loan.main.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ClosureGlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ClosureGlobalExceptionHandler.class);

    @ExceptionHandler(ClosureException.class)
    public ResponseEntity<Map<String, Object>> handleClosureException(
            ClosureException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("Closure validation failed: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        log.error("Unexpected closure error", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        return ResponseEntity.status(status).body(response);
    }
}
