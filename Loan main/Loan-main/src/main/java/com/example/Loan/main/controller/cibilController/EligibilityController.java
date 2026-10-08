package com.example.Loan.main.controller.cibilController;

import com.example.Loan.main.dto.cibilDto.ApiResponse;
import com.example.Loan.main.dto.cibilDto.CibilEligibiltyResponse;
import com.example.Loan.main.dto.cibilDto.RejectEligibilityRequest;
import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.exception.cibilExceptions.CustomerNotFound;
import com.example.Loan.main.respository.cibilRespository.customerRepository;
import com.example.Loan.main.service.cibilService.EligibilityService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eligibility")
@RequiredArgsConstructor
public class EligibilityController {


    private final EligibilityService eligibilityService;
    private final customerRepository customerRepository;


    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CibilEligibiltyResponse>> checkEligibility(
            @PathVariable Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFound("Customer not found"));
        CibilEligibiltyResponse response =
                eligibilityService.checkEligibility(customer);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Eligibility fetched successfully", response));
    }


    @GetMapping("/officer/pending")
    public ResponseEntity<ApiResponse<List<CibilEligibiltyResponse>>> getPendingEligibilities() {

        List<CibilEligibiltyResponse> response =
                eligibilityService.getPendingEligibilities();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Pending eligibilities fetched successfully", response));
    }


    @PutMapping("/officer/{eligibilityId}/approve")
    public ResponseEntity<ApiResponse<CibilEligibiltyResponse>>  approvedEligibilty(
            @PathVariable Long eligibilityId){

        CibilEligibiltyResponse response=eligibilityService.approvedEligibility(eligibilityId);

        return  ResponseEntity.ok(
                new ApiResponse<>(true, "Eligibility approved successfully", response));
    }

    @PutMapping("/officer/{eligibilityId}/reject")
    public ResponseEntity<ApiResponse<CibilEligibiltyResponse>> rejectEligibility(
            @PathVariable Long eligibilityId,
            @RequestBody RejectEligibilityRequest request) {

        CibilEligibiltyResponse response = eligibilityService.rejectEligibility(eligibilityId, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Eligibility rejected successfully", response)
        );
    }
}






