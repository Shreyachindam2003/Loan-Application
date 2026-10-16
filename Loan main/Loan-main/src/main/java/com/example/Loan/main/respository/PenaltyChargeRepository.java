package com.example.Loan.main.respository;

import java.util.List;

import com.example.Loan.main.entity.PenaltyCharge;
import com.example.Loan.main.enums.ChargeType;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PenaltyChargeRepository
        extends JpaRepository<PenaltyCharge, Integer> {

    List<PenaltyCharge> findByLoanAccountId(
            Integer loanAccountId);

    List<PenaltyCharge> findByEmiScheduleId(
            Integer emiScheduleId);

    List<PenaltyCharge> findByEmiScheduleIdAndChargeType(
            Integer emiScheduleId,
            ChargeType chargeType);

    boolean existsByEmiScheduleIdAndChargeType(
            Integer emiScheduleId, ChargeType chargeType);
}
