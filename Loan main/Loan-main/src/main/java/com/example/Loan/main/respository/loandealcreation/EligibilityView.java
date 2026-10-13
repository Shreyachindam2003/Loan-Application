package com.example.Loan.main.repository.loandealcreation;

import java.math.BigDecimal;

public interface EligibilityView {

    Integer getEligibilityId();

    Integer getCustomerId();

    Integer getCibilScore();

    Boolean getEligible();

    BigDecimal getLoanAmount();

    String getRiskCategory();
}//dada