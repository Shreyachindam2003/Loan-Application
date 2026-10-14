package com.example.Loan.main.dto.closure;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanClosureDto {

    private Integer loanAccountId;

    private String closureType;

    private BigDecimal finalSettlementAmount;

    private Integer closedBy;

    private String remarks;

    private String closureStatus;
}