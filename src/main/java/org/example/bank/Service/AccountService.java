package org.example.bank.Service;

import org.example.bank.Entity.Account;
import org.example.bank.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public Optional<Account> findAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
}