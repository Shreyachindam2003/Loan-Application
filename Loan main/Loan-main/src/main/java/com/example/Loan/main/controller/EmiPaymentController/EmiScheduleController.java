package com.example.Loan.main.controller.EmiPaymentController;

import java.util.List;

import com.example.Loan.main.dto.EmiPaymentDto.response.ApiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;
import com.example.Loan.main.service.EmiPaymentService.EmiScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "http://localhost:4200")

@RestController
@RequestMapping("/api/v1/emi-schedules")
@RequiredArgsConstructor
@Slf4j
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;

    // Get complete EMI schedule
    @GetMapping("/loan/{loanAccountId}")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> getEmiSchedule(
            @PathVariable Integer loanAccountId) {

        log.info("Fetching EMI schedule for loanAccountId: {}", loanAccountId);

        List<EmiScheduleResponse> data =
                emiScheduleService.getEmiSchedule(loanAccountId);

        log.info("EMI schedule fetched successfully for loanAccountId: {}, records: {}",
                loanAccountId, data.size());

        return ResponseEntity.ok(
                ApiResponse.<List<EmiScheduleResponse>>builder()
                        .success(true)
                        .message("EMI schedule fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Generate EMI schedule
    @PostMapping("/loan/{loanAccountId}/generate")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> generateEmiSchedule(
            @PathVariable Integer loanAccountId) {

        log.info("EMI schedule generation started for loanAccountId: {}",
                loanAccountId);

        List<EmiScheduleResponse> data =
                emiScheduleService.generateEmiSchedule(loanAccountId);

        log.info("EMI schedule generated successfully for loanAccountId: {}, records: {}",
                loanAccountId, data.size());

        return ResponseEntity.ok(
                ApiResponse.<List<EmiScheduleResponse>>builder()
                        .success(true)
                        .message("EMI schedule generated successfully")
                        .data(data)
                        .build()
        );
    }

    // Get single EMI
    @GetMapping("/{emiScheduleId}")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>> getEmiById(
            @PathVariable Integer emiScheduleId) {

        log.info("Fetching EMI details for emiScheduleId: {}", emiScheduleId);

        EmiScheduleResponse data =
                emiScheduleService.getEmiById(emiScheduleId);

        log.info("EMI details fetched successfully for emiScheduleId: {}",
                emiScheduleId);

        return ResponseEntity.ok(
                ApiResponse.<EmiScheduleResponse>builder()
                        .success(true)
                        .message("EMI details fetched successfully")
                        .data(data)
                        .build()
        );
    }


    @PostMapping("/loan/{loanAccountId}/recalculate")
    public ResponseEntity<ApiResponse<List<EmiScheduleResponse>>> recalculate(
            @PathVariable Integer loanAccountId) {

        log.info("EMI schedule recalculation started for loanAccountId: {}", loanAccountId);

        List<EmiScheduleResponse> data =
                emiScheduleService.recalculateEmiSchedule(loanAccountId);

        log.info("EMI schedule recalculated successfully for loanAccountId: {}, records: {}",
                loanAccountId, data.size());

        return ResponseEntity.ok(
                ApiResponse.<List<EmiScheduleResponse>>builder()
                        .success(true)
                        .message("EMI schedule recalculated successfully")
                        .data(data)
                        .build()
        );
    }

}
