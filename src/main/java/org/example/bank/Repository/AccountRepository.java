package org.example.bank.Repository;

import org.example.bank.Entity.Account;
import org.example.bank.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByUserId(Long Id);
}