package com.example.Loan.main.dto.EmiPaymentDto.response;

import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiScheduleResponse {

    private Integer emiScheduleId;
    private Integer installmentNo;
    private LocalDate dueDate;

    private BigDecimal emi;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;

    private EmiStatus status;
    private LocalDate paidDate;
}