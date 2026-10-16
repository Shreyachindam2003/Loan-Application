package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.DealDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.DealReviewResponse;
import com.example.Loan.main.dto.loandealcreation.response.LoanDealResponse;
import com.example.Loan.main.dto.loandealcreation.response.PageResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.DealReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DealReviewController {

    private final DealReviewService service;

    @PostMapping("/loan-deals/{dealId}/review")
    public ResponseApi<LoanDealResponse> review(
            @PathVariable("dealId") Integer dealId,
            @RequestParam("officerId") Integer officerId,
            @Valid @RequestBody DealDecisionRequest request
    ) {
        return ResponseApi.success(
                "Decision saved; notification created and email queued",
                service.review(dealId, officerId, request)
        );
    }

    @GetMapping("/deal-reviews/deal/{dealId}/latest")
    public ResponseApi<DealReviewResponse> getLatest(
            @PathVariable("dealId") Integer dealId
    ) {
        return ResponseApi.success(
                "Latest loan application review fetched",
                service.getLatestByDealId(dealId)
        );
    }

    @GetMapping("/deal-reviews/deal/{dealId}")
    public ResponseApi<PageResponse<DealReviewResponse>> getDealReviews(
            @PathVariable("dealId") Integer dealId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseApi.success(
                "Loan application review history fetched",
                service.getDealReviews(dealId, page, size)
        );
    }

    @GetMapping("/deal-reviews/officer/{officerId}")
    public ResponseApi<PageResponse<DealReviewResponse>> getOfficerReviews(
            @PathVariable("officerId") Integer officerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseApi.success(
                "Officer reviews fetched",
                service.getOfficerReviews(officerId, page, size)
        );
    }
}