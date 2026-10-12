package com.example.Loan.main.respository.EmiPaymentRepo;


import java.util.List;
import java.util.Optional;

import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import com.example.Loan.main.enums.EmiPaymentEnums.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;



public interface EmiScheduleRepository
        extends JpaRepository<EmiSchedule, Integer> {

    List<EmiSchedule> findByLoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId);

    Optional<EmiSchedule> findFirstByLoanAccountIdAndStatusOrderByInstallmentNoAsc(
            Integer loanAccountId,
            EmiStatus status);

    List<EmiSchedule> findByLoanAccountIdAndStatus(
            Integer loanAccountId,
            EmiStatus status);

    Optional<EmiSchedule> findByLoanAccountIdAndInstallmentNo(
            Integer loanAccountId,
            Integer installmentNo);

    List<EmiSchedule> findByStatus(EmiStatus status);
}