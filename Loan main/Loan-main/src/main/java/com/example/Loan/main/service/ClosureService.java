
package com.example.Loan.main.service;

import com.example.Loan.main.dto.ClosureResponseDto;
import com.example.Loan.main.dto.ForeClosureRequestDto;
import com.example.Loan.main.dto.LoanClosureDto;
import com.example.Loan.main.entity.ForeClosureRequest;
import com.example.Loan.main.entity.LoanClosure;

import java.util.List;



public interface ClosureService {

    ClosureResponseDto createRequest(ForeClosureRequestDto dto);

    List<ForeClosureRequest> getRequests();

    LoanClosure createClosure(LoanClosureDto dto);

    ClosureResponseDto approveRequest(Integer requestId);

    ClosureResponseDto rejectRequest(Integer requestId);
}
