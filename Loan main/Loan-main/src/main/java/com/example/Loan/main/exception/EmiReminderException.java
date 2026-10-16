package com.example.Loan.main.exception;

public class EmiReminderException extends RuntimeException {

    public EmiReminderException(String message) {
        super(message);
    }

    public EmiReminderException(String message, Throwable cause) {
        super(message, cause);
    }
}

