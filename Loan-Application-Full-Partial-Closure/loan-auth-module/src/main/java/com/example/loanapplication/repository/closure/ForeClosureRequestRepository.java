package com.example.loanapplication.repository.closure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.loanapplication.entity.closure.ForeClosureRequest;

@Repository
public interface ForeClosureRequestRepository
        extends JpaRepository<ForeClosureRequest, Integer> {

    List<ForeClosureRequest> findByLoanAccountId(Integer loanAccountId);
}