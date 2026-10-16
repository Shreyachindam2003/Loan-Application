package com.example.Loan.main.exception;

public class KycDocumentNotFoundException extends RuntimeException {
    public KycDocumentNotFoundException(String message) {
        super(message);
    }
}
