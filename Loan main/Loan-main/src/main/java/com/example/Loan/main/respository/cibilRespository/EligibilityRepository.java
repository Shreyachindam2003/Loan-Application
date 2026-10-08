package com.example.Loan.main.respository.cibilRespository;

import com.example.Loan.main.entity.cibilEntity.Customer;
import com.example.Loan.main.entity.cibilEntity.EligibilityResults;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EligibilityRepository extends JpaRepository<EligibilityResults,Long> {
    Optional<EligibilityResults> findByCustomer(Customer customer);
    Optional<EligibilityResults> findById(Long EligibilityId);
    List<EligibilityResults> findByStatus(String status);
}
