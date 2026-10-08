package com.example.Loan.main.service.cibilService;


import com.example.Loan.main.dto.cibilDto.CibilResponse;
import com.example.Loan.main.dto.cibilDto.CustomerCibilResponse;
import com.example.Loan.main.entity.cibilEntity.CibilReports;
import com.example.Loan.main.entity.cibilEntity.Customer;

public interface CibilService {

    CibilResponse calculateCibil(Customer customers);

    CustomerCibilResponse getCustomerDetails(Customer customers);

    CibilResponse getCibilScore(CibilReports cibilReports);





}
