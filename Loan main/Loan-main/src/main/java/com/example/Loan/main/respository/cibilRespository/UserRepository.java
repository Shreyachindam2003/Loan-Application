package com.example.Loan.main.respository.cibilRespository;


import com.example.Loan.main.entity.kycEntity.User;
import com.example.Loan.main.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRole(Role role);
}
