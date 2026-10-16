package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.EligibilityDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.EligibilityReviewResponse;

import java.util.List;

public interface EligibilityReviewService {

    List<EligibilityReviewResponse> getPendingReviews();

    EligibilityReviewResponse review(
            Integer eligibilityId,
            Integer officerId,
            EligibilityDecisionRequest request
    );
}