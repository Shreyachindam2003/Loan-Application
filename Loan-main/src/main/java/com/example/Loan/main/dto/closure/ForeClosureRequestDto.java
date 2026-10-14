package com.example.Loan.main.dto.closure;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ForeClosureRequestDto {

    private Integer loanAccountId;

    private String foreClosureType;

    private BigDecimal foreClosureAmount;

    private BigDecimal partialAmount;

    private Integer partialTenureMonths;

    private String reason;
}