package com.example.Loan.main.respository.cibilRespository;


import com.example.Loan.main.entity.cibilEntity.CibilReports;
import com.example.Loan.main.entity.cibilEntity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CibilRepository extends JpaRepository<CibilReports,Long> {

    Optional<CibilReports> findByCustomer(Customer customer);

}
