package com.example.Loan.main.serviceImpl;

import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.respository.CustomerRepository;
import com.example.Loan.main.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;

    public CustomerServiceImpl(CustomerRepository repository) {
        this.repository = repository;
    }

    public Customer getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }
}
