package com.example.Loan.main.service.EmiPaymentService;


import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyResponse;

import java.util.List;


public interface PenaltyService {

    List<PenaltyResponse> getPenalties(Integer loanAccountId);

    PenaltyResponse getPenalty(Integer penaltyChargeId);
}