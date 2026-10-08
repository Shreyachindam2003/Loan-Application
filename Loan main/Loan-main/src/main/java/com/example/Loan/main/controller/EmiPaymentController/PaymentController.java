package com.example.Loan.main.controller.EmiPaymentController;

import com.example.Loan.main.dto.EmiPaymentDto.request.CreatePaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.request.VerifyPaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.response.ApiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.CurrentEmiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.InvoiceResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PaymentHistoryResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PaymentResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyBreakupResponse;
import com.example.Loan.main.service.EmiPaymentService.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    // =========================================================
    // 1. GET CURRENT EMI
    // GET /api/v1/payments/loan/101/current-emi
    // =========================================================

    @GetMapping("/loan/{loanAccountId}/current-emi")
    public ResponseEntity<ApiResponse<CurrentEmiResponse>> getCurrentEmi(
            @PathVariable Integer loanAccountId) {

        CurrentEmiResponse response =
                paymentService.getCurrentEmi(loanAccountId);

        return ResponseEntity.ok(
                ApiResponse.<CurrentEmiResponse>builder()
                        .success(true)
                        .message("Current EMI fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 2. CREATE RAZORPAY ORDER
    // POST /api/v1/payments/create-order
    // =========================================================

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentOrder(
            @Valid @RequestBody CreatePaymentRequest request) {

        PaymentResponse response =
                paymentService.createPaymentOrder(request);

        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment order created successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 3. VERIFY RAZORPAY PAYMENT
    // POST /api/v1/payments/verify
    // =========================================================

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {

        PaymentResponse response =
                paymentService.verifyPayment(request);

        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment verified successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 4. GET PAYMENT BY ID
    // GET /api/v1/payments/1
    // =========================================================

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Integer paymentId) {

        PaymentResponse response =
                paymentService.getPaymentById(paymentId);

        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 5. PAYMENT HISTORY
    // GET /api/v1/payments/loan/101/history?page=0&size=10
    // =========================================================

    @GetMapping("/loan/{loanAccountId}/history")
    public ResponseEntity<ApiResponse<Page<PaymentHistoryResponse>>> getPaymentHistory(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<PaymentHistoryResponse> response =
                paymentService.getPaymentHistory(
                        loanAccountId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.<Page<PaymentHistoryResponse>>builder()
                        .success(true)
                        .message("Payment history fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 6. EMI PAYMENT BREAKUP
    // GET /api/v1/payments/emi/1/breakup
    // =========================================================

    @GetMapping("/emi/{emiScheduleId}/breakup")
    public ResponseEntity<ApiResponse<PenaltyBreakupResponse>> getPaymentBreakup(
            @PathVariable Integer emiScheduleId) {

        PenaltyBreakupResponse response =
                paymentService.getPaymentBreakup(emiScheduleId);

        return ResponseEntity.ok(
                ApiResponse.<PenaltyBreakupResponse>builder()
                        .success(true)
                        .message("Payment breakup fetched successfully")
                        .data(response)
                        .build()
        );
    }


    // =========================================================
    // 7. PAYMENT INVOICE
    // GET /api/v1/payments/1/invoice
    // =========================================================

    @GetMapping("/{paymentId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generatePaymentInvoice(
            @PathVariable Integer paymentId) {

        InvoiceResponse response =
                paymentService.generatePaymentInvoice(paymentId);

        return ResponseEntity.ok(
                ApiResponse.<InvoiceResponse>builder()
                        .success(true)
                        .message("Payment invoice generated successfully")
                        .data(response)
                        .build()
        );
    }
}
