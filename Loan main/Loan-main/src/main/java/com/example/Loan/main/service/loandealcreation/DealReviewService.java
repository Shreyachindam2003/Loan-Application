package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DealDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;

public interface DealReviewService {

    LoanDealResponse review(
            Integer dealId,
            Integer officerId,
            DealDecisionRequest request
    );
}//dada