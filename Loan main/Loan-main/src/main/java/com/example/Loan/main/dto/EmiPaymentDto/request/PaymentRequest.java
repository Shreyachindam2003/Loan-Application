package com.example.Loan.main.dto.EmiPaymentDto.request;

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

    // EMI, PARTIAL_CLOSURE or FULL_CLOSURE
    private String paymentPurpose;

    // Required for partial/full closure payments
    private Integer foreclosureRequestId;
}