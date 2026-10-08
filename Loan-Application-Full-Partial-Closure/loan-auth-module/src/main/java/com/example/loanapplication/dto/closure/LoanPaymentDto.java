package com.example.loanapplication.dto.closure;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LoanPaymentDto {

    private Integer loanAccountId;

    private BigDecimal paymentAmount;

    private String paymentStatus;

    private String paymentName;
}