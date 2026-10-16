package com.example.Loan.main.controller;

import java.util.List;

import com.example.Loan.main.dto.request.PaymentRequest;
import com.example.Loan.main.dto.response.ApiResponse;
import com.example.Loan.main.dto.response.CurrentEmiResponse;
import com.example.Loan.main.dto.response.PaymentBreakupResponse;
import com.example.Loan.main.dto.response.PaymentResponse;
import com.example.Loan.main.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "http://localhost:4200")

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // Get current payable EMI
    @GetMapping("/loan/{loanAccountId}/current-emi")
    public ResponseEntity<ApiResponse<CurrentEmiResponse>> getCurrentEmi(
            @PathVariable Integer loanAccountId) {

        log.info("Fetching current EMI for loanAccountId: {}", loanAccountId);

        CurrentEmiResponse data =
                paymentService.getCurrentEmi(loanAccountId);

        log.info("Current EMI fetched successfully for loanAccountId: {}",
                loanAccountId);

        return ResponseEntity.ok(
                ApiResponse.<CurrentEmiResponse>builder()
                        .success(true)
                        .message("Current EMI fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Create Razorpay order
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentResponse>> createOrder(
            @RequestBody PaymentRequest request) {

        log.info("Creating Razorpay order");

        PaymentResponse data = paymentService.createOrder(request);

        log.info("Razorpay order created successfully");

        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Razorpay order created successfully")
                        .data(data)
                        .build()
        );
    }

    // Verify Razorpay payment
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @RequestBody PaymentRequest request) {

        log.info("Razorpay payment verification requested");

        PaymentResponse data = paymentService.verifyPayment(request);

        log.info("Razorpay payment verified successfully");

        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment verified successfully")
                        .data(data)
                        .build()
        );
    }

    // Get payment details
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable Integer paymentId) {

        log.info("Fetching payment details for paymentId: {}", paymentId);

        PaymentResponse data = paymentService.getPayment(paymentId);

        log.info("Payment details fetched successfully for paymentId: {}",
                paymentId);

        return ResponseEntity.ok(
                ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment details fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Payment history
    @GetMapping("/loan/{loanAccountId}/history")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            @PathVariable Integer loanAccountId) {

        log.info("Fetching payment history for loanAccountId: {}",
                loanAccountId);

        List<PaymentResponse> data =
                paymentService.getPaymentHistory(loanAccountId);

        log.info("Payment history fetched successfully for loanAccountId: {}, count: {}",
                loanAccountId, data.size());

        return ResponseEntity.ok(
                ApiResponse.<List<PaymentResponse>>builder()
                        .success(true)
                        .message("Payment history fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // EMI + penalty + bonus breakup
    @GetMapping("/emi/{emiScheduleId}/breakup")
    public ResponseEntity<ApiResponse<PaymentBreakupResponse>> getPaymentBreakup(
            @PathVariable Integer emiScheduleId) {

        log.info("Fetching payment breakup for emiScheduleId: {}",
                emiScheduleId);

        PaymentBreakupResponse data =
                paymentService.getPaymentBreakup(emiScheduleId);

        log.info("Payment breakup fetched successfully for emiScheduleId: {}",
                emiScheduleId);

        return ResponseEntity.ok(
                ApiResponse.<PaymentBreakupResponse>builder()
                        .success(true)
                        .message("Payment breakup fetched successfully")
                        .data(data)
                        .build()
        );
    }

    // Payment invoice
    @GetMapping("/{paymentId}/invoice")
    public ResponseEntity<ApiResponse<PaymentBreakupResponse>> getInvoice(
            @PathVariable Integer paymentId) {

        log.info("Fetching payment invoice for paymentId: {}", paymentId);

        PaymentBreakupResponse data =
                paymentService.getInvoice(paymentId);

        log.info("Payment invoice fetched successfully for paymentId: {}",
                paymentId);

        return ResponseEntity.ok(
                ApiResponse.<PaymentBreakupResponse>builder()
                        .success(true)
                        .message("Payment invoice fetched successfully")
                        .data(data)
                        .build()
        );
    }
}
