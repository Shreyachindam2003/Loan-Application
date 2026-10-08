package com.example.Loan.main.service.EmiPaymentService;

public interface SchedulerService {

    void processEmiReminders();

    void processDailyPenalties();

    void checkOverdueLoans();
}
