package org.example.bank.Repository;

import org.example.bank.Entity.RefreshToken;
import org.example.bank.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
    Optional<RefreshToken> findByUserId(Long userId);
}
