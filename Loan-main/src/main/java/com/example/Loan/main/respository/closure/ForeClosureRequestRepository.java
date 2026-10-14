package com.example.Loan.main.respository.closure;

import com.example.Loan.main.entity.closure.ForeClosureRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForeClosureRequestRepository
        extends JpaRepository<ForeClosureRequest, Integer> {

    List<ForeClosureRequest> findByLoanAccountId(Integer loanAccountId);
}