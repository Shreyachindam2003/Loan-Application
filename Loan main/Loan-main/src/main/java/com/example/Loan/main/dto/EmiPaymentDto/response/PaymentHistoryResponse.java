package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {

    private Integer paymentId;

    private List<Integer> installmentNos;

    private BigDecimal emiAmount;

    private BigDecimal penaltyAmount;

    private BigDecimal bonusAmount;

    private BigDecimal totalAmount;

    private LocalDateTime paymentDate;

    private String paymentStatus;
}