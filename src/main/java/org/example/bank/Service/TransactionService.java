package org.example.bank.Service;

import org.example.bank.DTO.TransactionDTO;
import org.example.bank.Entity.Account;
import org.example.bank.Entity.Loan;
import org.example.bank.Entity.Transaction;
import org.example.bank.Entity.User;
import org.example.bank.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public Transaction createLoanRequestTransaction(Loan loan, User user) {
        Transaction transaction = new Transaction();
        transaction.setType("LOAN_CREATED");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(loan.getAmount());
        transaction.setDescription("Заявка на кредит на сумму " + loan.getAmount());
        transaction.setUser(user);
        transaction.setLoan(loan);
        return transactionRepository.save(transaction);
    }
    
    public Transaction createLoanApprovalTransaction(Loan loan, User user) {
        Transaction transaction = new Transaction();
        transaction.setType("LOAN_APPROVED");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(loan.getAmount());
        transaction.setDescription("Кредит одобрен. На счет зачислено " + loan.getAmount());
        transaction.setUser(user);
        transaction.setLoan(loan);
        return transactionRepository.save(transaction);
    }
    
    public Transaction createLoanPaymentTransaction(Loan loan, User user, Double paymentAmount) {
        Transaction transaction = new Transaction();
        transaction.setType("LOAN_PAYMENT");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(paymentAmount);
        transaction.setDescription("Платеж по кредиту. Сумма: " + paymentAmount);
        transaction.setUser(user);
        transaction.setLoan(loan);
        return transactionRepository.save(transaction);
    }
    
    public Transaction createLoanFullyPaidTransaction(Loan loan, User user) {
        Transaction transaction = new Transaction();
        transaction.setType("LOAN_PAID");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(0.0);
        transaction.setDescription("Кредит полностью погашен");
        transaction.setUser(user);
        transaction.setLoan(loan);
        return transactionRepository.save(transaction);
    }
    
    public Transaction createTransferTransaction(Account fromAccount, Account toAccount, Double amount) {
        Transaction transaction = new Transaction();
        transaction.setType("TRANSFER");
        transaction.setDate(LocalDateTime.now());
        transaction.setAmount(amount);
        transaction.setDescription("Перевод средств");
        transaction.setUser(fromAccount.getUser());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }
    
    public List<Transaction> getAccountTransactions(Account account) {
        List<Transaction> sentTransactions = transactionRepository.findByFromAccountOrderByDateDesc(account);
        List<Transaction> receivedTransactions = transactionRepository.findByToAccountOrderByDateDesc(account);
        
        // Объединяем два списка
        List<Transaction> allTransactions = sentTransactions;
        allTransactions.addAll(receivedTransactions);
        
        // Сортируем по дате (от новых к старым)
        return allTransactions.stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getLoanTransactions(Long loanId) {
        return transactionRepository.findByLoanId(loanId);
    }

    public TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType());
        dto.setDate(transaction.getDate());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        
        if (transaction.getUser() != null) {
            dto.setUserId(transaction.getUser().getId());
            dto.setUsername(transaction.getUser().getUsername());
        }
        
        if (transaction.getLoan() != null) {
            dto.setLoanId(transaction.getLoan().getId());
        }
        
        return dto;
    }
    
    public List<TransactionDTO> getUserTransactionsDTO(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TransactionDTO> getLoanTransactionsDTO(Long loanId) {
        return transactionRepository.findByLoanId(loanId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TransactionDTO> getAccountTransactionsDTO(Account account) {
        return getAccountTransactions(account).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}