package com.example.Loan.main.service.auth;


import com.example.Loan.main.entity.auth.Customer;

public interface CustomerService {
    Customer getByEmail(String email);
}
