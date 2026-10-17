package com.example.Loan.main.controller;

import java.util.List;

import com.example.Loan.main.config.ApiResponse;
import com.example.Loan.main.dto.response.PenaltyResponse;
import com.example.Loan.main.service.PenaltyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "http://localhost:4200")

@RestController
@RequestMapping("/api/v1/penalties")
@RequiredArgsConstructor
@Slf4j
public class PenaltyController {

    private final PenaltyService penaltyService;

    @GetMapping("/loan/{loanAccountId}")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getPenalties(@PathVariable Integer loanAccountId) {

        log.info("Fetching penalty and bonus charges for loanAccountId: {}",loanAccountId);

        List<PenaltyResponse> data =penaltyService.getPenalties(loanAccountId);

        log.info("Penalty and bonus charges fetched successfully for loanAccountId: {}, count: {}",
                loanAccountId, data.size());

        return ResponseEntity.ok(ApiResponse.<List<PenaltyResponse>>builder()
                        .success(true)
                        .message("Penalty and bonus charges fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Get single penalty/bonus detail
    @GetMapping("/{penaltyChargeId}")
    public ResponseEntity<ApiResponse<PenaltyResponse>> getPenalty(@PathVariable Integer penaltyChargeId) {

        log.info("Fetching penalty details for penaltyChargeId: {}",penaltyChargeId);

        PenaltyResponse data =penaltyService.getPenalty(penaltyChargeId);

        log.info("Penalty details fetched successfully for penaltyChargeId: {}",penaltyChargeId);

        return ResponseEntity.ok(ApiResponse.<PenaltyResponse>builder()
                        .success(true)
                        .message("Penalty details fetched successfully")
                        .data(data)
                        .build()
        );
    }
}
