package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DisbursementResponse(
        Integer disbursementId,
        Integer dealId,
        BigDecimal disbursedAmount,
        String bankPartner,
        String status,
        boolean simulated,
        LocalDateTime disbursementDate,
        LoanAccountResponse loanAccount
) {
}