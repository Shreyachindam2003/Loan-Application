package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DealDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.DealReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-deals")
@RequiredArgsConstructor
public class DealReviewController {

    private final DealReviewService service;

    @PostMapping("/{dealId}/review")
    public ResponseApi<LoanDealResponse> review(
            @PathVariable("dealId") Integer dealId,
            @RequestParam("officerId") Integer officerId,
            @Valid @RequestBody DealDecisionRequest request
    ) {
        return ResponseApi.success(
                "Decision saved; dashboard notification created and email queued",
                service.review(dealId, officerId, request)
        );
    }
}