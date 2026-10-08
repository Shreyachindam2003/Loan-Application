package com.example.Loan.main.respository.cibilRespository;

import com.example.Loan.main.entity.cibilEntity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface customerRepository extends JpaRepository<Customer,Long> {

    Optional<Customer> findById(Long CustomerId);

}
