package org.example.bank.Repository;

import org.example.bank.Entity.Account;
import org.example.bank.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByLoanId(Long loanId);
    List<Transaction> findByUserIdOrderByDateDesc(Long userId);
    List<Transaction> findByType(String type);
    List<Transaction> findByFromAccountOrToAccount(Account fromAccount, Account toAccount);
    List<Transaction> findByFromAccountOrderByDateDesc(Account fromAccount);
    List<Transaction> findByToAccountOrderByDateDesc(Account toAccount);
}