package com.example.Loan.main.service.EmiPaymentService;




import com.example.Loan.main.dto.EmiPaymentDto.request.PaymentRequest;
import com.example.Loan.main.dto.EmiPaymentDto.response.CurrentEmiResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PaymentBreakupResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PaymentResponse;

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
