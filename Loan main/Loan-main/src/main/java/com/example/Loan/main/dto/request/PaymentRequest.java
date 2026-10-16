package com.example.Loan.main.dto.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    private Integer loanAccountId;

    private Integer emiScheduleId;

    private BigDecimal amount;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    private String idempotencyKey;
}