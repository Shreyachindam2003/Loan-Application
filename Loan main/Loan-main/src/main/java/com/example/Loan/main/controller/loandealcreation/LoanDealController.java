package com.example.Loan.main.controller.loandealcreation;


import com.example.Loan.main.dto.loandealcreation.request.LoanApplyRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.LoanDealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-deals")
@RequiredArgsConstructor
public class LoanDealController {

    private final LoanDealService service;

    @PostMapping("/customers/{customerId}")
    public ResponseEntity<ResponseApi<LoanDealResponse>> apply(
            @PathVariable Integer customerId,
            @Valid @RequestBody LoanApplyRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseApi.success(
                        "Loan application submitted for officer review",
                        service.apply(customerId, request)
                ));
    }
    //dada
    @GetMapping("/pending")
    public ResponseEntity<ResponseApi<List<LoanDealResponse>>> pending() {
        return ResponseEntity.ok(
                ResponseApi.success(
                        "Pending loan applications fetched",
                        service.getPendingDeals()
                )
        );
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<ResponseApi<LoanDealResponse>> getById(
            @PathVariable Integer dealId
    ) {
        return ResponseEntity.ok(
                ResponseApi.success(
                        "Loan application fetched",
                        service.getById(dealId)
                )
        );
    }
}
