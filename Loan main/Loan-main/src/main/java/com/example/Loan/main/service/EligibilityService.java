package com.example.Loan.main.service;

import com.example.Loan.main.dto.CibilEligibiltyResponse;
import com.example.Loan.main.dto.RejectEligibilityRequest;
import com.example.Loan.main.entity.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EligibilityService {

    public CibilEligibiltyResponse checkEligibility(Customer customer);
    public CibilEligibiltyResponse approvedEligibility(Integer eligibilityId);
    public CibilEligibiltyResponse rejectEligibility(Integer eligibilityId, RejectEligibilityRequest request);
    List<CibilEligibiltyResponse> getPendingEligibilities();




}
