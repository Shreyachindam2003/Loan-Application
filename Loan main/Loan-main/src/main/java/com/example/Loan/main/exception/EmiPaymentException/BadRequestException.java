package com.example.Loan.main.exception.EmiPaymentException;


public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}