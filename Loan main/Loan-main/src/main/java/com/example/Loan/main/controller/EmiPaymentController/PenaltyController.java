package com.example.Loan.main.controller.EmiPaymentController;

import com.example.Loan.main.dto.EmiPaymentDto.response.ApiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyDetailResponse;
import com.example.Loan.main.service.EmiPaymentService.PenaltyService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;


    // =========================================================
    // 1. GET PENALTIES
    // GET /api/v1/penalties/loan/101?page=0&size=10
    // =========================================================

    @GetMapping("/loan/{loanAccountId}")
    public ResponseEntity<ApiResponse<Page<PenaltyResponse>>> getPenalties(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<PenaltyResponse> response =
                penaltyService.getPenalties(
                        loanAccountId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.<Page<PenaltyResponse>>builder()
                        .success(true)
                        .message("Penalty details fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 2. GET PENALTY BY ID
    // GET /api/v1/penalties/1
    // =========================================================

    @GetMapping("/{penaltyChargeId}")
    public ResponseEntity<ApiResponse<PenaltyDetailResponse>> getPenaltyById(
            @PathVariable Integer penaltyChargeId) {

        PenaltyDetailResponse response =
                penaltyService.getPenaltyById(
                        penaltyChargeId
                );

        return ResponseEntity.ok(
                ApiResponse.<PenaltyDetailResponse>builder()
                        .success(true)
                        .message("Penalty detail fetched successfully")
                        .data(response)
                        .build()
        );
    }
}
