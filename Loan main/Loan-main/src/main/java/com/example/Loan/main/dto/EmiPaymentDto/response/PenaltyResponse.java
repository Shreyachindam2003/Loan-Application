package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyResponse {

    private Integer penaltyChargeId;

    private Integer emiScheduleId;

    private Integer installmentNo;

    private LocalDate dueDate;

    private BigDecimal penaltyAmount;

    private BigDecimal bonusAmount;

    private BigDecimal totalCharges;

    private String status;
}
