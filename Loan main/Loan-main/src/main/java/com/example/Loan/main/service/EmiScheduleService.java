package com.example.Loan.main.service;


import com.example.Loan.main.dto.response.EmiScheduleResponse;

import java.util.List;



public interface EmiScheduleService {

    List<EmiScheduleResponse> getEmiSchedule(Integer loanAccountId);

    EmiScheduleResponse getEmiById(Integer emiScheduleId);

    List<EmiScheduleResponse> generateEmiSchedule(Integer loanAccountId);

    List<EmiScheduleResponse> recalculateEmiSchedule(
            Integer loanAccountId);

}
