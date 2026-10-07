package com.example.loanapplication.controller;

import com.example.loanapplication.entity.Customer;
import com.example.loanapplication.service.CustomerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
