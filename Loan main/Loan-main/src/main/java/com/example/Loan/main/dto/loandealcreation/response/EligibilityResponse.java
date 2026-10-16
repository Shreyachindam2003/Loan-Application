
package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;

public record EligibilityResponse(
        Integer eligibilityId,
        Integer customerId,
        Integer cibilScore,
        Boolean eligible,
        BigDecimal eligibleLoanAmount,
        String riskCategory,
        String reviewStatus
) {
}
