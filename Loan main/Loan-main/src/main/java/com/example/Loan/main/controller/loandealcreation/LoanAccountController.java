package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.LoanAccountResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.DisbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-accounts")
@RequiredArgsConstructor
public class LoanAccountController {
    //dada
    private final DisbursementService service;

    @GetMapping("/{loanAccountId}")
    public ResponseApi<LoanAccountResponse> getById(
            @PathVariable("loanAccountId") Integer loanAccountId
    ) {
        return ResponseApi.success(
                "Loan account fetched successfully",
                service.getAccount(loanAccountId)
        );
    }

    @GetMapping("/customers/{customerId}")
    public ResponseApi<List<LoanAccountResponse>> getCustomerAccounts(
            @PathVariable("customerId") Integer customerId
    ) {
        return ResponseApi.success(
                "Customer loan accounts fetched",
                service.getCustomerAccounts(customerId)
        );
    }
}