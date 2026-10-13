package com.example.Loan.main.dto.closure;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LoanPaymentDto {

    private Integer loanAccountId;

    private BigDecimal paymentAmount;

    private String paymentStatus;

    private String paymentName;
}