
package com.example.Loan.main.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LoanPaymentDto {

    private Integer loanAccountId;

    private BigDecimal amount;

    private String paymentType;

    private String idempotencyKey;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;
}
