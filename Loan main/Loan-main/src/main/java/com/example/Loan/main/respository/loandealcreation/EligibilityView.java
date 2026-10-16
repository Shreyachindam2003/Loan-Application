package com.example.Loan.main.respository.loandealcreation;

import java.math.BigDecimal;

public interface EligibilityView {

    Integer getEligibilityId();

    Integer getCustomerId();

    Integer getCibilScore();

    Boolean getEligible();

    BigDecimal getLoanAmount();

    String getReviewStatus();
    
    String getRiskCategory();
}