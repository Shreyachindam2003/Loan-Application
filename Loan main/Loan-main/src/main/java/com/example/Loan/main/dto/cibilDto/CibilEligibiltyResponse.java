package com.example.Loan.main.dto.cibilDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class CibilEligibiltyResponse{

    private BigDecimal loanAmount;
    private String FullName;
    private String panNo;
    private Integer Score;
    private BigDecimal monthlyIncome;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String  RejectionReason;
    private String status;






}



