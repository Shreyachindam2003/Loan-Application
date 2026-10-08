package com.example.Loan.main.respository.EmiPaymentRepo;


import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PenaltyChargeRepository
        extends JpaRepository<PenaltyCharge, Integer> {

    List<PenaltyCharge> findByEmiScheduleIdOrderByChargeDateAsc(
            Integer emiScheduleId
    );

    Page<PenaltyCharge> findByLoanAccountIdOrderByPenaltyChargeIdDesc(
            Integer loanAccountId,
            Pageable pageable
    );

    List<PenaltyCharge> findByEmiScheduleIdAndChargeType(
            Integer emiScheduleId,
            String chargeType
    );

    @Query("""
        SELECT COALESCE(SUM(p.penaltyAmount), 0)
        FROM PenaltyCharge p
        WHERE p.emiScheduleId = :emiScheduleId
        AND p.chargeType = :chargeType
        AND p.status = 'PENDING'
    """)
    BigDecimal getPendingChargeAmount(
            @Param("emiScheduleId") Integer emiScheduleId,
            @Param("chargeType") String chargeType
    );

    boolean existsByEmiScheduleIdAndChargeTypeAndChargeDate(
            Integer emiScheduleId,
            String chargeType,
            LocalDate chargeDate
    );

    @Query("""
        SELECT p
        FROM PenaltyCharge p
        WHERE p.emiScheduleId = :emiScheduleId
        AND p.status = 'PENDING'
        ORDER BY p.chargeDate ASC
    """)
    List<PenaltyCharge> findPendingCharges(
            @Param("emiScheduleId") Integer emiScheduleId
    );
}
