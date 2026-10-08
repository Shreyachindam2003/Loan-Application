package com.example.loanapplication.respository.EmiPaymentRepo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loanapplication.entity.EmiPaymentEntity.EmiSchedule;

@Repository
public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer> {

    List<EmiSchedule> findByLoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId);
}