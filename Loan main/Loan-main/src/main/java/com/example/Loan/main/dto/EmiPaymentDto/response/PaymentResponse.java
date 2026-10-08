package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Integer paymentId;

    private Integer loanAccountId;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentDate;

    private String paymentStatus;

    private String paymentType;

    private String razorpayOrderId;

    private String razorpayPaymentId;
}
