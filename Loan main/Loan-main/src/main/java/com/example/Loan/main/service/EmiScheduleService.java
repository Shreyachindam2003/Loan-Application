package com.example.Loan.main.service;


import com.example.Loan.main.dto.response.EmiScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



public interface EmiScheduleService {

    List<EmiScheduleResponse> getEmiSchedule(Integer loanAccountId);

//    Page<EmiScheduleResponse> getEmiSchedule(Integer loanId, Pageable pageable);

    EmiScheduleResponse getEmiById(Integer emiScheduleId);

    List<EmiScheduleResponse> generateEmiSchedule(Integer loanAccountId);

    List<EmiScheduleResponse> recalculateEmiSchedule(
            Integer loanAccountId);

}
