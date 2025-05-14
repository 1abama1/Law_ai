package org.example.bank.Service;

import org.example.bank.Entity.Account;
import org.example.bank.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionService transactionService;

    public Optional<Account> findAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
    
    @Transactional
    public void transferMoney(Account fromAccount, Account toAccount, Double amount) {
        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }
        
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // Создаем запись о транзакции
        transactionService.createTransferTransaction(fromAccount, toAccount, amount);
    }
}