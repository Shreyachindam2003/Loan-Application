package com.example.Loan.main.dto.EmiPaymentDto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ForeClosureRequest {

    private Integer loanAccountId;

    private String foreClosureType;

    private BigDecimal foreClosureAmount;

    private BigDecimal partialAmount;

//    private Integer partialTenureMonths;

    private String reason;
}