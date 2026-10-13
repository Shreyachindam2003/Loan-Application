package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.entity.loandealcreation.LoanDeal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanDealRepository
        extends JpaRepository<LoanDeal, Integer> {

    List<LoanDeal> findByStatusOrderByCreatedAtAsc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from LoanDeal d where d.dealId = :dealId")
    Optional<LoanDeal> findByIdForUpdate(
            @Param("dealId") Integer dealId
    );
}//dada