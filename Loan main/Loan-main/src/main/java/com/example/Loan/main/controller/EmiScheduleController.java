package com.example.Loan.main.controller;


import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.entity.EmiSchedule;
import com.example.Loan.main.entity.LoanAccount;
import com.example.Loan.main.respository.EmiScheduleRepository;
import com.example.Loan.main.respository.LoanAccountRepository;
import com.example.Loan.main.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emi-schedules")
public class EmiScheduleController {

    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final CustomerService customerService;

    public EmiScheduleController(
            EmiScheduleRepository emiScheduleRepository,
            LoanAccountRepository loanAccountRepository,
            CustomerService customerService) {
        this.emiScheduleRepository = emiScheduleRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.customerService = customerService;
    }

    @GetMapping("/my")
    public List<EmiSchedule> getMyEmiSchedules(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first"
            );
        }

        Customer customer =
                customerService.getByEmail(authentication.getName());

        if (customer == null || customer.getCustomerId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer profile not found"
            );
        }

        List<LoanAccount> accounts =
                loanAccountRepository.findByCustomerId(
                        customer.getCustomerId()
                );

        if (accounts.isEmpty()) {
            return List.of();
        }

        List<Integer> loanAccountIds = accounts.stream()
                .map(LoanAccount::getLoanAccountId)
                .toList();

        return emiScheduleRepository
                .findByLoanAccountIdInOrderByDueDateAsc(
                        loanAccountIds
                );
    }
}
