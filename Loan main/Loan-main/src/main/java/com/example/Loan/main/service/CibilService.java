package com.example.Loan.main.service;


import com.example.Loan.main.dto.CibilResponse;
import com.example.Loan.main.dto.CustomerCibilResponse;
import com.example.Loan.main.entity.CibilReports;
import com.example.Loan.main.entity.Customer;

public interface CibilService {

    CibilResponse calculateCibil(Customer customers);

    CustomerCibilResponse getCustomerDetails(Customer customers);

    CibilResponse getCibilScore(CibilReports cibilReports);





}
