package com.example.Loan.main.exception;

public class KycDocumentAlreadyApprovedException
        extends RuntimeException {

    public KycDocumentAlreadyApprovedException(String message) {
        super(message);
    }
}