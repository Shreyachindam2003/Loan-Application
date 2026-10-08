package com.example.Loan.main.respository.EmiPaymentRepo;

import com.example.Loan.main.entity.EmiPaymentEntity.EmiSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Integer> {

    List<EmiSchedule> findByLoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId
    );

    Page<EmiSchedule> findByLoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId,
            Pageable pageable
    );

    Optional<EmiSchedule> findByLoanAccountIdAndInstallmentNo(
            Integer loanAccountId,
            Integer installmentNo
    );

    Optional<EmiSchedule> findFirstByLoanAccountIdAndPaymentStatusOrderByDueDateAsc(
            Integer loanAccountId,
            String paymentStatus
    );

    long countByLoanAccountIdAndPaymentStatus(
            Integer loanAccountId,
            String paymentStatus
    );

    @Query("""
    SELECT e
    FROM EmiSchedule e
    WHERE e.paymentStatus = :status
    AND e.dueDate BETWEEN :startDate AND :endDate
    ORDER BY e.dueDate ASC
""")
    List<EmiSchedule> findPendingEmisBetweenDates(
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT e
        FROM EmiSchedule e
        WHERE e.paymentStatus = 'PENDING'
        AND e.dueDate < :today
        ORDER BY e.dueDate ASC
    """)
    List<EmiSchedule> findOverdueEmis(
            @Param("today") LocalDate today
    );
}