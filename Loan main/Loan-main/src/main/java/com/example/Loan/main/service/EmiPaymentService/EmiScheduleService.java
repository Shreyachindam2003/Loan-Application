package com.example.Loan.main.service.EmiPaymentService;

import com.example.Loan.main.dto.EmiPaymentDto.response.EmiDashboardResponse;
import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmiScheduleService {

    EmiDashboardResponse getEmiDashboard(Integer loanAccountId);

    Integer getEmiDay(Integer loanAccountId);

    void generateSchedule(Integer loanAccountId);

    Page<EmiScheduleResponse> getSchedulePage(
            Integer loanAccountId,
            Pageable pageable
    );

    EmiScheduleResponse getScheduleById(Integer emiScheduleId);

    void recalculateAfterPartialForeclosure(Integer loanAccountId);

    void closeLoanAfterFullForeclosure(Integer loanAccountId);
}

