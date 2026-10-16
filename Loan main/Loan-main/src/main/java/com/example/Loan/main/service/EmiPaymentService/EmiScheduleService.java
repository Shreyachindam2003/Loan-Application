package com.example.Loan.main.service.EmiPaymentService;


import com.example.Loan.main.dto.EmiPaymentDto.response.EmiScheduleResponse;

import java.util.List;



public interface EmiScheduleService {

    List<EmiScheduleResponse> getEmiSchedule(Integer loanAccountId);

    EmiScheduleResponse getEmiById(Integer emiScheduleId);

    List<EmiScheduleResponse> generateEmiSchedule(Integer loanAccountId);

    List<EmiScheduleResponse> recalculateEmiSchedule(
            Integer loanAccountId);

}
