package com.example.Loan.main.service;




import com.example.Loan.main.dto.request.PaymentRequest;
import com.example.Loan.main.dto.response.CurrentEmiResponse;
import com.example.Loan.main.dto.response.PaymentBreakupResponse;
import com.example.Loan.main.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    CurrentEmiResponse getCurrentEmi(Integer loanAccountId);

    PaymentResponse createOrder(PaymentRequest request);

    PaymentResponse verifyPayment(PaymentRequest request);

    PaymentResponse getPayment(Integer paymentId);

    List<PaymentResponse> getPaymentHistory(Integer loanAccountId);

    PaymentBreakupResponse getPaymentBreakup(Integer emiScheduleId);

    PaymentBreakupResponse getInvoice(Integer paymentId);
}
