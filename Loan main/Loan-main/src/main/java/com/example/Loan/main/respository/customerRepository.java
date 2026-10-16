package com.example.Loan.main.respository;

import com.example.Loan.main.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface customerRepository extends JpaRepository<Customer,Integer> {

    Optional<Customer> findById(Integer CustomerId);

}
