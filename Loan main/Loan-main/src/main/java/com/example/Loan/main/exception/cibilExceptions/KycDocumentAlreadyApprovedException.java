package com.example.Loan.main.exception.cibilExceptions;

public class KycDocumentAlreadyApprovedException
        extends RuntimeException {

    public KycDocumentAlreadyApprovedException(String message) {
        super(message);
    }
}