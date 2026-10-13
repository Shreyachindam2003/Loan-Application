package com.example.Loan.main.respository;

import java.util.List;

import com.example.Loan.main.dto.EmiPaymentDto.ForeClosureRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ForeClosureRequestRepository
        extends JpaRepository<ForeClosureRequest, Integer> {

    List<ForeClosureRequest> findByLoanAccountId(Integer loanAccountId);

    List<ForeClosureRequest>
    findByLoanAccountIdAndForeClosureTypeIgnoreCaseAndIsPaidTrue(
            Integer loanAccountId, String foreClosureType);
}