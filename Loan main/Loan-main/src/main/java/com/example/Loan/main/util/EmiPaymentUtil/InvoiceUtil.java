package com.example.Loan.main.util.EmiPaymentUtil;

import org.springframework.stereotype.Component;

@Component
public class InvoiceUtil {

    public String generateInvoiceNumber(Integer paymentId) {
        return "INV-" + paymentId;
    }
}