package com.example.Loan.main.service;


import com.example.Loan.main.entity.Customer;

public interface CustomerService {
    Customer getByEmail(String email);
}
