package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DisbursementRequest;
import com.example.Loan.main.dto.loandealcreation.response.DisbursementDetailsResponse;
import com.example.Loan.main.dto.loandealcreation.response.DisbursementResponse;
import com.example.Loan.main.dto.loandealcreation.response.LoanAccountResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;

import java.util.List;

public interface DisbursementService {

    DisbursementResponse disburse(
            Integer dealId,
            DisbursementRequest request
    );

    DisbursementDetailsResponse getDisbursementById(
            Integer disbursementId
    );

    DisbursementDetailsResponse getLatestDisbursementByDealId(
            Integer dealId
    );

    PageResponse<DisbursementDetailsResponse> getDealDisbursements(
            Integer dealId,
            int page,
            int size
    );

    LoanAccountResponse getAccount(Integer loanAccountId);

    List<LoanAccountResponse> getCustomerAccounts(
            Integer customerId
    );

    LoanAccountResponse getAccountByDealId(Integer dealId);

    LoanAccountResponse getLatestAccountByCustomerId(
            Integer customerId
    );

    PageResponse<LoanAccountResponse> getCustomerAccountPage(
            Integer customerId,
            int page,
            int size
    );
}