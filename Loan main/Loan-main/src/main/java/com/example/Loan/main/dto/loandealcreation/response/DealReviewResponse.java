package com.example.Loan.main.dto.loandealcreation.response;

import java.time.LocalDateTime;

public record DealReviewResponse(
        Integer dealId,
        Integer officerId,
        String status,
        String remarks,
        LocalDateTime reviewedAt
) {
}