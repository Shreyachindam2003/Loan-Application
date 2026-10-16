package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanAccountResponse(
        Integer loanAccountId,
        String loanAccountNo,
        Integer customerId,
        Integer dealId,
        BigDecimal loanAmount,
        BigDecimal outstandingPrincipal,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal emiAmount,
        BigDecimal totalPaidAmount,
        String status,
        LocalDateTime disbursementDate
) {
}