package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentEmiResponse {

    private Integer emiScheduleId;

    private String loanAccountNo;

    private Integer installmentNo;

    private BigDecimal emiAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal bonusAmount;
    private BigDecimal totalAmount;

    private LocalDate dueDate;

    private String status;

    private boolean canPay;
}
