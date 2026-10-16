package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.LoanAccountResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.DisbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-accounts")
@RequiredArgsConstructor
public class LoanAccountController {

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

    @GetMapping("/deal/{dealId}")
    public ResponseApi<LoanAccountResponse> getByDealId(
            @PathVariable("dealId") Integer dealId
    ) {
        return ResponseApi.success(
                "Application loan account fetched",
                service.getAccountByDealId(dealId)
        );
    }

    @GetMapping("/customer/{customerId}/latest")
    public ResponseApi<LoanAccountResponse> getLatest(
            @PathVariable("customerId") Integer customerId
    ) {
        return ResponseApi.success(
                "Latest customer loan account fetched",
                service.getLatestAccountByCustomerId(customerId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseApi<PageResponse<LoanAccountResponse>> getCustomerAccountPage(
            @PathVariable("customerId") Integer customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseApi.success(
                "Customer loan accounts fetched",
                service.getCustomerAccountPage(customerId, page, size)
        );
    }
}