package com.example.Loan.main.dto.closure;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanPaymentDto {

    private Integer loanAccountId;

    private BigDecimal paymentAmount;

    private String paymentStatus;

    private String paymentName;
}