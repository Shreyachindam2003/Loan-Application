package com.example.Loan.main.exception;

public class EmiNotFoundException extends RuntimeException {
    public EmiNotFoundException(String message) {
        super(message);
    }
}
