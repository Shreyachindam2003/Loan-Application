package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SanctionLetterResponse(
        Integer sanctionId,
        String sanctionNumber,
        Integer dealId,
        String applicationNumber,
        String applicantName,
        String mobileNo,
        String loanType,
        BigDecimal sanctionedAmount,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal emiAmount,
        BigDecimal processingChargePercent,
        BigDecimal processingChargeAmount,
        BigDecimal originationFee,
        Integer validityDays,
        LocalDateTime issuedAt,
        LocalDateTime validUntil,
        String propertyAddress,
        String issuingBank,
        List<String> additionalConditions,
        String authorizedSignatory
) {
}