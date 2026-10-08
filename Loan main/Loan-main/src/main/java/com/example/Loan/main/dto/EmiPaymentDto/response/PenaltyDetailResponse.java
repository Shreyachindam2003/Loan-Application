package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyDetailResponse {

    private LocalDate chargeDate;

    private Integer daysLate;

    private BigDecimal amount;

    private String reason;
}
