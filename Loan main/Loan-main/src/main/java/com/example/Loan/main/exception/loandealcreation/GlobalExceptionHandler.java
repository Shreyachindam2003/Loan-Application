package com.example.Loan.main.exception.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(
        basePackages = "com.example.Loan.main.controller.loandealcreation"
)
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseApi<Void>> notFound(
            ResourceNotFoundException exception
    ) {
        return failure(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            BusinessException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ResponseApi<Void>> badRequest(
            RuntimeException exception
    ) {
        return failure(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseApi<Map<String, String>>> validation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        return ResponseEntity.badRequest()
                .body(ResponseApi.failure(
                        "Validation failed",
                        errors
                ));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ResponseApi<Void>> invalidRequest(
            Exception exception
    ) {
        return failure(
                HttpStatus.BAD_REQUEST,
                "Invalid request body or request parameter"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseApi<Void>> conflict(
            DataIntegrityViolationException exception
    ) {
        log.warn("Loan module database constraint failed", exception);

        return failure(
                HttpStatus.CONFLICT,
                "Database constraint failed; check duplicate or linked records"
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseApi<Void>> unknownEndpoint(
            NoResourceFoundException exception
    ) {
        return failure(
                HttpStatus.NOT_FOUND,
                "API endpoint not found"
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseApi<Void>> unsupportedMethod(
            HttpRequestMethodNotSupportedException exception
    ) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(exception.getHeaders())
                .body(ResponseApi.<Void>failure(
                        "HTTP method is not supported",
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseApi<Void>> unexpected(
            Exception exception
    ) {
        log.error("Loan module request failed", exception);

        return failure(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred"
        );
    }

    private ResponseEntity<ResponseApi<Void>> failure(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status)
                .body(ResponseApi.<Void>failure(
                        message,
                        null
                ));
    }
}//dada