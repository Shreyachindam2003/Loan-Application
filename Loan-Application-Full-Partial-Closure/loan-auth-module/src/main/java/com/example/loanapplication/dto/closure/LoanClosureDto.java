package com.example.loanapplication.dto.closure;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LoanClosureDto {

    private Integer loanAccountId;

    private String closureType;

    private BigDecimal finalSettlementAmount;

    private Integer closedBy;

    private String remarks;

    private String closureStatus;
}