package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DisbursementRequest;
import com.example.Loan.main.dto.loandealcreation.response.DisbursementResponse;
import com.example.Loan.main.dto.loandealcreation.response.LoanAccountResponse;

import java.util.List;

public interface DisbursementService {

    DisbursementResponse disburse(
            Integer dealId,
            DisbursementRequest request
    );

    LoanAccountResponse getAccount(Integer loanAccountId);

    List<LoanAccountResponse> getCustomerAccounts(
            Integer customerId
    );
}//dada