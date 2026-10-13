package com.example.Loan.main.service.auth;

import com.example.loanapplication.entity.auth.Customer;

public interface CustomerService {
    Customer getByEmail(String email);
}
