package com.example.Loan.main.service.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.EligibilityResponse;

public interface EligibilityService {

    EligibilityResponse getLatestByCustomerId(Integer customerId);
}