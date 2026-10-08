package com.example.Loan.main.dto.EmiPaymentDto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyBreakupResponse {

    private BigDecimal emiAmount;

    private BigDecimal penaltyAmount;

    private BigDecimal bonusAmount;

    private BigDecimal totalAmount;

    private List<PenaltyDetailResponse> penaltyDetails;

    private List<PenaltyDetailResponse> bonusDetails;
}