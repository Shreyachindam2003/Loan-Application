package com.example.loanapplication.repository.auth;

import com.example.loanapplication.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUser_UserId(Integer userId);
}
