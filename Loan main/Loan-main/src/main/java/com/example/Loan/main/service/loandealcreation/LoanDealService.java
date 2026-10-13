package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.LoanApplyRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;

import java.util.List;

public interface LoanDealService {

    LoanDealResponse apply(
            Integer customerId,
            LoanApplyRequest request
    );

    LoanDealResponse getById(Integer dealId);

    List<LoanDealResponse> getPendingDeals();
}