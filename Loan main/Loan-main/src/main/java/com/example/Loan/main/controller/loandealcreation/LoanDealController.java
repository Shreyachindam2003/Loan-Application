package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.LoanApplyRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.enums.loandealcreation.LoanApplicationStatus;
import com.example.Loan.main.service.loandealcreation.LoanDealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-deals")
@RequiredArgsConstructor
public class LoanDealController {

    private final LoanDealService service;

    @PostMapping("/customers/{customerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseApi<LoanDealResponse> apply(
            @PathVariable("customerId") Integer customerId,
            @Valid @RequestBody LoanApplyRequest request
    ) {
        return ResponseApi.success(
                "Loan application submitted for officer review",
                service.apply(customerId, request)
        );
    }

    @GetMapping("/{dealId}")
    public ResponseApi<LoanDealResponse> getById(
            @PathVariable("dealId") Integer dealId
    ) {
        return ResponseApi.success(
                "Loan application fetched",
                service.getById(dealId)
        );
    }

    @GetMapping("/pending")
    public ResponseApi<List<LoanDealResponse>> getPendingDeals() {
        return ResponseApi.success(
                "Pending loan applications fetched",
                service.getPendingDeals()
        );
    }

    @GetMapping("/customer/{customerId}/latest")
    public ResponseApi<LoanDealResponse> getLatest(
            @PathVariable("customerId") Integer customerId
    ) {
        return ResponseApi.success(
                "Latest customer loan application fetched",
                service.getLatestByCustomerId(customerId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseApi<PageResponse<LoanDealResponse>> getCustomerDeals(
            @PathVariable("customerId") Integer customerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseApi.success(
                "Customer loan applications fetched",
                service.getCustomerDeals(customerId, page, size)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseApi<PageResponse<LoanDealResponse>> getDealsByStatus(
            @PathVariable("status") LoanApplicationStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseApi.success(
                "Loan applications fetched by status",
                service.getDealsByStatus(status, page, size)
        );
    }
}