package com.example.Loan.main.exception.cibilExceptions;


import com.example.Loan.main.dto.cibilDto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(CustomerNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomerNotFound(
            CustomerNotFound ex) {

        log.warn("Customer not found: {}", ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }




    @ExceptionHandler(CibilReportNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleCibilReportNotFound(
            CibilReportNotFound ex) {

        log.warn("CIBIL report not found: {}", ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }




    @ExceptionHandler(Eligibiltynotfound.class)
    public ResponseEntity<ApiResponse<Void>> handleEligibilityNotFound(
            Eligibiltynotfound ex) {

        log.warn("Eligibility result not found: {}", ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }



    @ExceptionHandler(rejectNotEmpty.class)
    public ResponseEntity<ApiResponse<Void>> handleRejectReasonEmpty(
            rejectNotEmpty ex) {

        log.warn("Rejection validation failed: {}", ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {

        log.error("Unexpected error occurred", ex);
        ApiResponse<Void> response = new ApiResponse<>(false, "Something went wrong", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(EligibilityStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleEligibilityStatusException(EligibilityStatusException ex) {

        log.warn("Eligibility status validation failed: {}", ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(KycDocumentAlreadyApprovedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyApproved(
            KycDocumentAlreadyApprovedException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
}