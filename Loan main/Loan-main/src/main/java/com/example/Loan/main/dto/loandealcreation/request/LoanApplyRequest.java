package com.example.Loan.main.dto.loandealcreation.request;

import com.example.Loan.main.enums.loandealcreation.LoanType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanApplyRequest(

        @NotNull(message = "Scorecard ID is required")
        @Positive(message = "Scorecard ID must be positive")
        Integer scoreCardId,
//dada
        @NotNull(message = "Loan type is required")
        LoanType loanType,

        @NotNull(message = "Loan amount is required")
        @DecimalMin(value = "0.01", message = "Loan amount must be positive")
        @Digits(integer = 16, fraction = 2)
        BigDecimal loanAmount,

        @NotNull(message = "Tenure is required")
        @Min(value = 1, message = "Tenure must be at least one month")
        @Max(value = 360, message = "Tenure cannot exceed 360 months")
        Integer tenureMonths,

        @NotBlank(message = "Bank name is required")
        @Size(max = 200)
        String bankName,

        @NotBlank(message = "Bank account number is required")
        @Pattern(
                regexp = "[0-9]{6,50}",
                message = "Account number must contain 6 to 50 digits"
        )
        String bankAccountNumber,

        @NotBlank(message = "IFSC code is required")
        @Pattern(
                regexp = "[A-Z]{4}0[A-Z0-9]{6}",
                message = "Enter a valid uppercase IFSC code"
        )
        String ifscCode,

        @NotNull(message = "Requested disbursement date is required")
        @FutureOrPresent(message = "Requested date cannot be in the past")
        LocalDate requestedDisbursementDate,

        @NotNull(message = "EMI day is required")
        @Min(value = 1, message = "EMI day must be between 1 and 28")
        @Max(value = 28, message = "EMI day must be between 1 and 28")
        Integer emiDay,

        @Size(max = 1000, message = "Property address is too long")
        String propertyAddress
) {
}