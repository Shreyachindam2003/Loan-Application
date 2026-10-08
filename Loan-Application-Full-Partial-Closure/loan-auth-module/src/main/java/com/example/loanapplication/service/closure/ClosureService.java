package com.example.loanapplication.service.closure;

import java.util.List;

import com.example.loanapplication.dto.closure.ClosureResponseDto;
import com.example.loanapplication.dto.closure.ForeClosureRequestDto;
import com.example.loanapplication.dto.closure.LoanClosureDto;
import com.example.loanapplication.dto.closure.LoanPaymentDto;
import com.example.loanapplication.entity.closure.ForeClosureRequest;
import com.example.loanapplication.entity.closure.LoanClosure;
import com.example.loanapplication.entity.closure.LoanPayment;

public interface ClosureService {

    ClosureResponseDto createRequest(ForeClosureRequestDto dto);

    List<ForeClosureRequest> getRequests();

    LoanPayment createPayment(LoanPaymentDto dto);

    LoanClosure createClosure(LoanClosureDto dto);
}