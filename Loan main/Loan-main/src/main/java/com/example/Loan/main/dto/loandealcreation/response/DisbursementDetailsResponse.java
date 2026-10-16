package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DisbursementDetailsResponse(
        Integer disbursementId,
        Integer dealId,
        BigDecimal disbursedAmount,
        String bankPartner,
        String status,
        LocalDateTime disbursementDate
) {
}