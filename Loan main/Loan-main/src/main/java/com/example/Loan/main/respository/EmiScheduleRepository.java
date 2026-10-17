package com.example.Loan.main.respository;


import com.example.Loan.main.entity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer> {

    List<EmiSchedule> findByLoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId
    );

    List<EmiSchedule> findByLoanAccountIdInOrderByDueDateAsc(
            List<Integer> loanAccountIds
    );
}
