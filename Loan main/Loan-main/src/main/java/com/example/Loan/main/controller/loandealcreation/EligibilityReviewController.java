package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.request.EligibilityDecisionRequest;
import com.example.Loan.main.dto.loandealcreation.response.EligibilityReviewResponse;
import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.service.loandealcreation.EligibilityReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eligibility")
@RequiredArgsConstructor
public class EligibilityReviewController {

    private final EligibilityReviewService service;

    @GetMapping("/pending")
    public ResponseApi<List<EligibilityReviewResponse>> getPendingReviews() {

        return ResponseApi.success(
                "Pending eligibility reviews fetched",
                service.getPendingReviews()
        );
    }

    @PostMapping("/{eligibilityId}/review")
    public ResponseApi<EligibilityReviewResponse> review(
            @PathVariable Integer eligibilityId,
            @RequestParam Integer officerId,
            @Valid @RequestBody EligibilityDecisionRequest request
    ) {

        return ResponseApi.success(
                "Eligibility decision saved",
                service.review(
                        eligibilityId,
                        officerId,
                        request
                )
        );
    }
}