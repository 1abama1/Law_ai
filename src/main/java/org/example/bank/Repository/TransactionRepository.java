package org.example.bank.Repository;

import org.example.bank.Entity.Account;
import org.example.bank.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountOrToAccount(Account from, Account to);
}