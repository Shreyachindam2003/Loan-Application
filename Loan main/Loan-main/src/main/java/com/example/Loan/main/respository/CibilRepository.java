package com.example.Loan.main.respository;


import com.example.Loan.main.entity.CibilReports;
import com.example.Loan.main.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CibilRepository extends JpaRepository<CibilReports,Integer> {

    Optional<CibilReports> findByCustomer(Customer customer);

}
