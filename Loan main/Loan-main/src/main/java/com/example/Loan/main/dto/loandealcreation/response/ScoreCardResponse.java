package com.example.Loan.main.dto.loandealcreation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScoreCardResponse(
        Integer scoreCardId,
        Integer customerId,
        String riskCategory,
        BigDecimal eligibleLoanAmount,
        Integer cibilScore,
        String status,
        LocalDateTime generatedAt
) {
}//dada