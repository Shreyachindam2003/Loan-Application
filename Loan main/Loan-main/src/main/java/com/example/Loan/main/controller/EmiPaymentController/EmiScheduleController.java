package com.example.Loan.main.controller.EmiPaymentController;

import com.example.Loan.main.dto.EmiPaymentDto.response.ApiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.EmiDashboardResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;
import com.example.Loan.main.service.EmiPaymentService.EmiScheduleService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/emi-schedules")
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;


    // =========================================================
    // 1. EMI DASHBOARD
    // GET /api/v1/emi-schedules/loan/101
    // =========================================================

    @GetMapping("/loan/{loanAccountId}")
    public ResponseEntity<ApiResponse<EmiDashboardResponse>> getEmiDashboard(
            @PathVariable Integer loanAccountId) {

        EmiDashboardResponse response =
                emiScheduleService.getEmiDashboard(loanAccountId);

        return ResponseEntity.ok(
                ApiResponse.<EmiDashboardResponse>builder()
                        .success(true)
                        .message("EMI dashboard fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 2. GET EMI DAY
    // GET /api/v1/emi-schedules/loan/101/emi-day
    // =========================================================

    @GetMapping("/loan/{loanAccountId}/emi-day")
    public ResponseEntity<ApiResponse<Integer>> getEmiDay(
            @PathVariable Integer loanAccountId) {

        Integer emiDay =
                emiScheduleService.getEmiDay(loanAccountId);

        return ResponseEntity.ok(
                ApiResponse.<Integer>builder()
                        .success(true)
                        .message("EMI day fetched successfully")
                        .data(emiDay)
                        .build()
        );
    }


    // =========================================================
    // 3. GENERATE EMI SCHEDULE
    // POST /api/v1/emi-schedules/loan/101/generate
    // =========================================================

    @PostMapping("/loan/{loanAccountId}/generate")
    public ResponseEntity<ApiResponse<Void>> generateSchedule(
            @PathVariable Integer loanAccountId) {

        emiScheduleService.generateSchedule(loanAccountId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("EMI schedule generated successfully")
                        .data(null)
                        .build()
        );
    }


    // =========================================================
    // 4. NORMAL OFFSET PAGINATION
    // GET /api/v1/emi-schedules/loan/101/page?page=0&size=10
    // =========================================================

    @GetMapping("/loan/{loanAccountId}/page")
    public ResponseEntity<ApiResponse<Page<EmiScheduleResponse>>> getSchedulePage(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<EmiScheduleResponse> response =
                emiScheduleService.getSchedulePage(
                        loanAccountId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.<Page<EmiScheduleResponse>>builder()
                        .success(true)
                        .message("EMI schedule fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 5. GET EMI BY ID
    // GET /api/v1/emi-schedules/1
    // =========================================================

    @GetMapping("/{emiScheduleId}")
    public ResponseEntity<ApiResponse<EmiScheduleResponse>> getScheduleById(
            @PathVariable Integer emiScheduleId) {

        EmiScheduleResponse response =
                emiScheduleService.getScheduleById(emiScheduleId);

        return ResponseEntity.ok(
                ApiResponse.<EmiScheduleResponse>builder()
                        .success(true)
                        .message("EMI schedule fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 6. RECALCULATE AFTER PARTIAL FORECLOSURE
    // POST /api/v1/emi-schedules/loan/101/recalculate
    // =========================================================

    @PostMapping("/loan/{loanAccountId}/recalculate")
    public ResponseEntity<ApiResponse<Void>> recalculateAfterPartialForeclosure(
            @PathVariable Integer loanAccountId) {

        emiScheduleService.recalculateAfterPartialForeclosure(
                loanAccountId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(
                                "EMI schedule recalculated successfully after partial foreclosure"
                        )
                        .data(null)
                        .build()
        );
    }


    // =========================================================
    // 7. CLOSE AFTER FULL FORECLOSURE
    // POST /api/v1/emi-schedules/loan/101/close
    // =========================================================

    @PostMapping("/loan/{loanAccountId}/close")
    public ResponseEntity<ApiResponse<Void>> closeLoanAfterFullForeclosure(
            @PathVariable Integer loanAccountId) {

        emiScheduleService.closeLoanAfterFullForeclosure(
                loanAccountId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Loan closed successfully after full foreclosure")
                        .data(null)
                        .build()
        );
    }
}
