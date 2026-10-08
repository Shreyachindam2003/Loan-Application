package com.example.loanapplication.dto.closure;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ClosureResponseDto {

    private Integer requestId;

    private Integer loanAccountId;

    private String foreClosureType;

    private BigDecimal partialAmount;

    private Integer partialTenureMonths;

    private BigDecimal revisedEmiAmount;

    private BigDecimal remainingPrincipal;

    private String status;

    private String message;
}