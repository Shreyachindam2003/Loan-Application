package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiDashboardResponse {

    private String loanAccountNo;

    private BigDecimal loanAmount;
    private BigDecimal emiAmount;

    private Integer totalEmis;
    private Integer paidEmis;
    private Integer pendingEmis;

    private BigDecimal paidAmount;
    private BigDecimal remainingBalance;

    private List<EmiScheduleResponse> schedule;
}
