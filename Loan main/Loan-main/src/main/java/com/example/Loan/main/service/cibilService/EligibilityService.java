package com.example.Loan.main.service.cibilService;

import com.example.Loan.main.dto.cibilDto.CibilEligibiltyResponse;
import com.example.Loan.main.dto.cibilDto.RejectEligibilityRequest;
import com.example.Loan.main.entity.cibilEntity.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EligibilityService {

    public CibilEligibiltyResponse checkEligibility(Customer customer);
    public CibilEligibiltyResponse approvedEligibility(Long eligibilityId);
    public CibilEligibiltyResponse rejectEligibility(Long eligibilityId, RejectEligibilityRequest request);
    List<CibilEligibiltyResponse> getPendingEligibilities();




}
