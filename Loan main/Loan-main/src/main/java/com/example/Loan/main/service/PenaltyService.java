package com.example.Loan.main.service;


import com.example.Loan.main.dto.response.PenaltyResponse;

import java.util.List;


public interface PenaltyService {

    List<PenaltyResponse> getPenalties(Integer loanAccountId);

    PenaltyResponse getPenalty(Integer penaltyChargeId);
}