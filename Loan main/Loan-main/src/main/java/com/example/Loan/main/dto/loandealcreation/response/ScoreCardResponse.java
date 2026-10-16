package com.example.Loan.main.dto.loandealcreation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCardResponse {

    private Integer scoreCardId;
    private Integer customerId;
    private String riskCategory;
    private BigDecimal eligibleLoanAmount;
    private Integer cibilScore;
    private String status;
    private LocalDateTime generatedAt;
}