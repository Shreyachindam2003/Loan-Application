package com.example.Loan.main.exception.cibilExceptions;

public class KycFileException extends RuntimeException {
    public KycFileException(String message) {
        super(message);
    }
    public KycFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
