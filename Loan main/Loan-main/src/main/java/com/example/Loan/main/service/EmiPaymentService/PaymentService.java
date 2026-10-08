package com.example.Loan.main.service.EmiPaymentService;


import com.example.Loan.main.dto.EmiPaymentDto.request.CreatePaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.request.VerifyPaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

    CurrentEmiResponse getCurrentEmi(Integer loanAccountId);

    PaymentResponse createPaymentOrder(CreatePaymentRequest request);

    PaymentResponse verifyPayment(VerifyPaymentRequest request);

    PaymentResponse getPaymentById(Integer paymentId);

    Page<PaymentHistoryResponse> getPaymentHistory(
            Integer loanAccountId,
            Pageable pageable
    );

    PenaltyBreakupResponse getPaymentBreakup(Integer emiScheduleId);

    InvoiceResponse generatePaymentInvoice(Integer paymentId);
}