package com.example.Loan.main.dto.loandealcreation.request;

import jakarta.validation.constraints.NotNull;

public record DisbursementRequest(

        @NotNull(message = "Bank partner is required")
        BankPartner bankPartner
) {
    //dada
    public enum BankPartner {
        SBI,
        HDFC,
        ICICI,
        AXIS,
        BANK_OF_INDIA,
        BANK_OF_MAHARASHTRA
    }
}