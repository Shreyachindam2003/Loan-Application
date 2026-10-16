package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EligibilityReviewResponse(
        Integer eligibilityId,
        Integer customerId,
        Integer cibilScore,
        Boolean eligible,
        BigDecimal eligibleLoanAmount,
        String riskCategory,
        String reviewStatus,
        String rejectionReason,
        Integer reviewedBy,
        LocalDateTime reviewedAt
) {
}