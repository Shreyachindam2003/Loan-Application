package com.example.Loan.main.service.EmiPaymentService;


import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyBreakupResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyDetailResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PenaltyService {

    Page<PenaltyResponse> getPenalties(
            Integer loanAccountId,
            Pageable pageable
    );

    PenaltyBreakupResponse getPenaltyBreakup(Integer emiScheduleId);

    PenaltyDetailResponse getPenaltyById(Integer penaltyChargeId);
}
