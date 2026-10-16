package com.example.Loan.main.respository;

import com.example.Loan.main.entity.Customer;
import com.example.Loan.main.entity.EligibilityResults;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EligibilityRepository extends JpaRepository<EligibilityResults,Integer> {
    Optional<EligibilityResults> findByCustomer(Customer customer);
    Optional<EligibilityResults> findById(Integer EligibilityId);
    List<EligibilityResults> findByStatus(String status);
}
