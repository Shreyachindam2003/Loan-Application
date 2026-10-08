package com.example.loanapplication.dto.closure;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ForeClosureRequestDto {

    private Integer loanAccountId;

    private String foreClosureType;

    private BigDecimal foreClosureAmount;

    private BigDecimal partialAmount;

    private Integer partialTenureMonths;

    private String reason;
}