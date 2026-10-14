package com.example.Loan.main.service.closure;

import com.example.Loan.main.dto.closure.ClosureResponseDto;
import com.example.Loan.main.dto.closure.ForeClosureRequestDto;
import com.example.Loan.main.dto.closure.LoanClosureDto;
import com.example.Loan.main.dto.closure.LoanPaymentDto;
import com.example.Loan.main.entity.closure.ForeClosureRequest;
import com.example.Loan.main.entity.closure.LoanClosure;
import com.example.Loan.main.entity.closure.LoanPayment;

import java.util.List;


public interface ClosureService {

    ClosureResponseDto createRequest(ForeClosureRequestDto dto);

    List<ForeClosureRequest> getRequests();

    LoanPayment createPayment(LoanPaymentDto dto);

    LoanClosure createClosure(LoanClosureDto dto);
}