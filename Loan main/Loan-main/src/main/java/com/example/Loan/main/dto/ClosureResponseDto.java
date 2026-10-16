
package com.example.Loan.main.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClosureResponseDto {

    private Integer requestId;

    private Integer loanAccountId;

    private String foreClosureType;

    private BigDecimal foreClosureAmount;

    private BigDecimal partialAmount;

    private LocalDateTime requestedDate;

    private String status;

    private String message;
}
