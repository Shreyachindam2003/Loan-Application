package com.example.Loan.main.respository;


import com.example.Loan.main.entity.User;
import com.example.Loan.main.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByRole(Role role);
}
