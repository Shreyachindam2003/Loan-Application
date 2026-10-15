package com.example.Loan.main.dto.EmiPaymentDto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentEmiResponse {

    private Integer emiScheduleId;
    private Integer installmentNo;
    private LocalDate dueDate;

    private BigDecimal emiAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal bonusAmount;
    private BigDecimal totalPayable;

    private Integer daysLate;
    private EmiStatus status;
}