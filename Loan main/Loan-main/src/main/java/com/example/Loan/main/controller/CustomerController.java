package com.example.Loan.main.controller;


import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public Customer getMyProfile(Authentication authentication) {
        return service.getByEmail(authentication.getName());
    }
}
