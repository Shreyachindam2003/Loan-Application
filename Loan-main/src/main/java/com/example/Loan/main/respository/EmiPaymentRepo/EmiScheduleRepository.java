package com.example.Loan.main.respository.EmiPaymentRepo;

import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer> {

    List<EmiSchedule> findByLoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId);
}