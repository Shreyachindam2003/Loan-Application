package com.example.Loan.main.dto.loandealcreation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DealDecisionRequest(

        @NotNull(message = "Decision is required")
        Decision decision,

        @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
        String remarks
) {
    //dada
    public enum Decision {
        APPROVED,
        REJECTED
    }
}