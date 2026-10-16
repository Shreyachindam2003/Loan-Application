package com.example.Loan.main.dto.response;


import java.math.BigDecimal;
import java.time.LocalDate;


import com.example.Loan.main.enums.EmiStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmiScheduleResponse {

    private Integer emiScheduleId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private BigDecimal openingBalance;
    private BigDecimal emiAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal closingBalance;
    private EmiStatus status;
}