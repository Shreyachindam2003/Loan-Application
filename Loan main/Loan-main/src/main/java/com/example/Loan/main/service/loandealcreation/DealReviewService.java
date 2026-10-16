package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DealDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.DealReviewResponse;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;

public interface DealReviewService {

    LoanDealResponse review(
            Integer dealId,
            Integer officerId,
            DealDecisionRequest request
    );

    DealReviewResponse getLatestByDealId(Integer dealId);

    PageResponse<DealReviewResponse> getDealReviews(
            Integer dealId,
            int page,
            int size
    );

    PageResponse<DealReviewResponse> getOfficerReviews(
            Integer officerId,
            int page,
            int size
    );
}