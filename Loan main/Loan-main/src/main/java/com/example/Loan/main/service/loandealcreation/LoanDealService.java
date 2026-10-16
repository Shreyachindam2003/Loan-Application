package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.LoanApplyRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;
import com.example.Loan.main.enums.loandealcreation.LoanApplicationStatus;

import java.util.List;

public interface LoanDealService {

    LoanDealResponse apply(
            Integer customerId,
            LoanApplyRequest request
    );

    LoanDealResponse getById(Integer dealId);

    List<LoanDealResponse> getPendingDeals();

    LoanDealResponse getLatestByCustomerId(Integer customerId);

    PageResponse<LoanDealResponse> getCustomerDeals(
            Integer customerId,
            int page,
            int size
    );

    PageResponse<LoanDealResponse> getDealsByStatus(
            LoanApplicationStatus status,
            int page,
            int size
    );
}