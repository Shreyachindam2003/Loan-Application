package com.example.Loan.main.controller.cibilController;

import com.example.Loan.main.dto.cibilDto.ApiResponse;
import com.example.Loan.main.dto.cibilDto.CibilResponse;
import com.example.Loan.main.dto.cibilDto.CustomerCibilResponse;
import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.exception.cibilExceptions.CustomerNotFound;
import com.example.Loan.main.respository.cibilRespository.customerRepository;
import com.example.Loan.main.service.cibilService.CibilService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cibil")
@RequiredArgsConstructor
public class CibilController {

    private final CibilService cibilService;
    private final customerRepository customerRepository;


    @PostMapping("/customer/{customerId}/calculate")
    public ResponseEntity<ApiResponse<CibilResponse>> calculateCibil(
            @PathVariable Long customerId) {

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
            @PathVariable Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFound("Customer not found"));

        CustomerCibilResponse response = cibilService.getCustomerDetails(customer);
        ApiResponse<CustomerCibilResponse> apiResponse =
                new ApiResponse<>(true, "Customer details fetched successfully", response);

        return ResponseEntity.ok(apiResponse);
    }
}