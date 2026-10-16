package com.example.Loan.main.controller;

import com.example.Loan.main.dto.ApiResponse;
import com.example.Loan.main.dto.CibilResponse;
import com.example.Loan.main.dto.CustomerCibilResponse;
import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.exception.CustomerNotFound;
import com.example.Loan.main.respository.customerRepository;
import com.example.Loan.main.service.CibilService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cibil")
@CrossOrigin("http://localhost:4200/")
@RequiredArgsConstructor
public class CibilController {

    private final CibilService cibilService;
    private final customerRepository customerRepository;


    @PostMapping("/customer/{customerId}/calculate")
    public ResponseEntity<ApiResponse<CibilResponse>> calculateCibil(
            @PathVariable Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFound("Customer not found"));

        CibilResponse response = cibilService.calculateCibil(customer);

        ApiResponse<CibilResponse> apiResponse =
                new ApiResponse<>(true, "CIBIL score calculated successfully", response);
        return ResponseEntity.ok(apiResponse);
    }



    @GetMapping("/customer/{customerId}/details")
    public ResponseEntity<ApiResponse<CustomerCibilResponse>> getCustomerDetails(
            @PathVariable Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFound("Customer not found"));

        CustomerCibilResponse response = cibilService.getCustomerDetails(customer);
        ApiResponse<CustomerCibilResponse> apiResponse =
                new ApiResponse<>(true, "Customer details fetched successfully", response);


        return ResponseEntity.ok(apiResponse);
    }
}