package org.example.bank.Repository;

import org.example.bank.Entity.UserBlock;
import org.example.bank.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    Optional<UserBlock> findByUser(User user);
    Optional<UserBlock> findByUserId(Long userId);
    List<UserBlock> findByExpiryDateBefore(LocalDateTime now);
    List<UserBlock> findByExpiryDateAfter(LocalDateTime now);
} 