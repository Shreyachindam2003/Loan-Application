package com.example.Loan.main.exception;

public class KycCustomerNotFoundException extends RuntimeException {
    public KycCustomerNotFoundException(String message) {
        super(message);
    }
}
