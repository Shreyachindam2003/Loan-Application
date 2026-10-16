package com.example.Loan.main.dto.EmiPaymentDto.response;


import java.math.BigDecimal;
import java.time.LocalDate;


import com.example.Loan.main.enums.EmiPaymentEnums.ChargeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PenaltyResponse {

    private Integer penaltyChargeId;
    private Integer emiScheduleId;
    private ChargeType chargeType;
    private BigDecimal amount;
    private LocalDate chargeDate;
    private Integer daysLate;

    // Calculated totals for My Penalties page
    private BigDecimal penaltyAmount;
    private BigDecimal bonusAmount;
}
