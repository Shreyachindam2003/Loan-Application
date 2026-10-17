package com.example.Loan.main.respository;


import com.example.Loan.main.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUser_UserId(Integer userId);
}
