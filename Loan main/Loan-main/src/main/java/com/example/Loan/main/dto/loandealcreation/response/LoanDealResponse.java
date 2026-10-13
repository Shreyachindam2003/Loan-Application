package com.example.Loan.main.dto.loandealcreation.response;

import com.example.Loan.main.enums.loandealcreation.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
//dada
public record LoanDealResponse(
        Integer dealId,
        Integer customerId,
        Integer scoreCardId,
        LoanType loanType,
        BigDecimal loanAmount,
        BigDecimal eligibleLoanAmount,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal emiAmount,
        LocalDate requestedDisbursementDate,
        Integer emiDay,
        String status,
        LocalDateTime createdAt
) {
}