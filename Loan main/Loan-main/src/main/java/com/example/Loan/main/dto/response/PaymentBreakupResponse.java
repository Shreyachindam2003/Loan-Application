package com.example.Loan.main.dto.response;


import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentBreakupResponse {

    private Integer emiScheduleId;
    private Integer installmentNo;
    private LocalDate dueDate;

    private BigDecimal emiAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal bonusAmount;
    private BigDecimal totalAmount;

    private Integer daysLate;


}
