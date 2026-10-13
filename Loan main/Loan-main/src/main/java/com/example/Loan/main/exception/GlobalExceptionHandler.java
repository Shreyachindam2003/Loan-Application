package com.example.Loan.main.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404 - EMI not found
    @ExceptionHandler(EmiNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmiNotFound(
            EmiNotFoundException ex) {
        log.warn("EMI not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 404 - Payment not found
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 404 - Notification not found
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(
            NotificationNotFoundException ex) {
        log.warn("Notification not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 404 - Other resources not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400 - Invalid loan data
    @ExceptionHandler(InvalidLoanDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLoanData(
            InvalidLoanDataException ex) {
        log.warn("Invalid loan data: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 - Invalid payment
    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayment(
            InvalidPaymentException ex) {
        log.warn("Invalid payment: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 - Invalid notification type
    @ExceptionHandler(InvalidNotificationTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNotificationType(
            InvalidNotificationTypeException ex) {
        log.warn("Invalid notification type: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 - Invalid loan closure request
    @ExceptionHandler(LoanClosureException.class)
    public ResponseEntity<ErrorResponse> handleLoanClosure(
            LoanClosureException ex) {
        log.warn("Loan closure error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 500 - EMI reminder processing failure
    @ExceptionHandler(EmiReminderException.class)
    public ResponseEntity<ErrorResponse> handleEmiReminder(
            EmiReminderException ex) {
        log.error("EMI reminder processing failed: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "EMI reminder processing failed.");
    }

    // 500 - Payment processing failure
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessing(
            PaymentProcessingException ex) {
        log.error("Payment processing failed: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Payment processing failed. Please try again later.");
    }

    // 409 - Conflicting state or duplicate operation
    // Use this handler only if ResourceConflictException exists
    // and extends RuntimeException.

    // 400 - Other runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
    }

    // 404 - Penalty not found
    @ExceptionHandler(PenaltyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePenaltyNotFound(
            PenaltyNotFoundException ex) {

        log.warn("Penalty charge not found: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    // 500 - Unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex) {
        log.error("Unexpected exception occurred", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message) {

        ErrorResponse response = new ErrorResponse();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(status)
                .body(response);
    }
}

