package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private Integer paymentId;

    private String loanAccountNo;

    private String customerName;

    private BigDecimal emiAmount;

    private BigDecimal penaltyAmount;

    private BigDecimal bonusAmount;

    private BigDecimal totalAmount;

    private LocalDateTime paymentDate;

    private String paymentStatus;
}
