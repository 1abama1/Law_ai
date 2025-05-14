package org.example.bank.Service;

import org.example.bank.DTO.LoanDTO;
import org.example.bank.Entity.Account;
import org.example.bank.Entity.Loan;
import org.example.bank.Entity.LoanStatus;
import org.example.bank.Entity.User;
import org.example.bank.Repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanStatusService loanStatusService;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private TransactionService transactionService;

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
    
    public List<LoanDTO> getAllLoansDTO() {
        return loanRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<Loan> getLoansByStatus(String statusName) {
        return loanRepository.findByStatusName(statusName);
    }
    
    public List<LoanDTO> getLoansByStatusDTO(String statusName) {
        return loanRepository.findByStatusName(statusName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<LoanDTO> getLoansByUserIdDTO(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Loan createLoan(Loan loan) {
        validateLoan(loan);
        LoanStatus pendingStatus = loanStatusService.getLoanStatusByName("PENDING");
        if (pendingStatus == null) {
            throw new RuntimeException("Loan status 'PENDING' not found in database");
        }
        loan.setStatus(pendingStatus);
        loan.setStartDate(LocalDateTime.now());
        loan.setEndDate(loan.getStartDate().plusMonths(loan.getTermInMonths()));
        loan.setRemainingAmount(loan.calculateTotalAmount());
        
        Loan savedLoan = loanRepository.save(loan);
        
        // Создание транзакции для заявки на кредит
        transactionService.createLoanRequestTransaction(savedLoan, loan.getUser());
        
        return savedLoan;
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Transactional
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        // Проверяем, что кредит в статусе PENDING
        LoanStatus pendingStatus = loanStatusService.getLoanStatusByName("PENDING");
        if (loan.getStatus().getId() != pendingStatus.getId()) {
            throw new RuntimeException("Loan is not in pending status");
        }

        // Получаем счет пользователя
        User user = loan.getUser();
        Account account = user.getAccount();
        
        if (account == null) {
            throw new RuntimeException("User does not have an account");
        }
        
        // Зачисляем сумму кредита на счет пользователя
        account.setBalance(account.getBalance() + loan.getAmount());
        accountService.saveAccount(account);
        
        // Меняем статус кредита на активный
        LoanStatus activeStatus = loanStatusService.getLoanStatusByName("ACTIVE");
        if (activeStatus == null) {
            throw new RuntimeException("Loan status 'ACTIVE' not found in database");
        }
        loan.setStatus(activeStatus);
        
        Loan approvedLoan = loanRepository.save(loan);
        
        // Создание транзакции для одобрения кредита
        transactionService.createLoanApprovalTransaction(approvedLoan, user);
        
        return approvedLoan;
    }

    @Transactional
    public Loan makePayment(Long loanId, Double paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        LoanStatus activeStatus = loanStatusService.getLoanStatusByName("ACTIVE");
        if (loan.getStatus().getId() != activeStatus.getId()) {
            throw new RuntimeException("Loan is not active");
        }

        if (paymentAmount > loan.getRemainingAmount()) {
            throw new RuntimeException("Payment amount exceeds remaining loan amount");
        }

        // Списываем средства со счета пользователя
        User user = loan.getUser();
        Account account = user.getAccount();
        
        if (account == null) {
            throw new RuntimeException("User does not have an account");
        }
        
        if (account.getBalance() < paymentAmount) {
            throw new RuntimeException("Insufficient funds in account");
        }
        
        // Списываем деньги со счета
        account.setBalance(account.getBalance() - paymentAmount);
        accountService.saveAccount(account);
        
        // Уменьшаем оставшуюся сумму кредита
        loan.setRemainingAmount(loan.getRemainingAmount() - paymentAmount);
        
        // Создание транзакции для платежа по кредиту
        transactionService.createLoanPaymentTransaction(loan, user, paymentAmount);
        
        // Если кредит полностью погашен, меняем статус
        if (loan.getRemainingAmount() <= 0) {
            LoanStatus paidStatus = loanStatusService.getLoanStatusByName("PAID");
            if (paidStatus == null) {
                throw new RuntimeException("Loan status 'PAID' not found in database");
            }
            loan.setStatus(paidStatus);
            
            // Создание транзакции для полного погашения кредита
            transactionService.createLoanFullyPaidTransaction(loan, user);
        }
        
        return loanRepository.save(loan);
    }
    
    /**
     * Метод для автоматического списания ежемесячного платежа
     * Этот метод должен запускаться по расписанию через Spring Scheduler
     */
    @Transactional
    public void processMonthlyPayments() {
        LoanStatus activeStatus = loanStatusService.getLoanStatusByName("ACTIVE");
        if (activeStatus == null) {
            throw new RuntimeException("Loan status 'ACTIVE' not found in database");
        }
        
        // Получаем все активные кредиты
        List<Loan> activeLoans = loanRepository.findByStatusId(activeStatus.getId());
        
        for (Loan loan : activeLoans) {
            try {
                // Рассчитываем ежемесячный платеж
                Double monthlyPayment = loan.calculateMonthlyPayment();
                
                User user = loan.getUser();
                Account account = user.getAccount();
                
                // Если на счете достаточно средств
                if (account != null && account.getBalance() >= monthlyPayment) {
                    account.setBalance(account.getBalance() - monthlyPayment);
                    accountService.saveAccount(account);
                    
                    // Уменьшаем оставшуюся сумму кредита
                    loan.setRemainingAmount(loan.getRemainingAmount() - monthlyPayment);
                    
                    // Если кредит полностью выплачен
                    if (loan.getRemainingAmount() <= 0) {
                        LoanStatus paidStatus = loanStatusService.getLoanStatusByName("PAID");
                        loan.setStatus(paidStatus);
                    }
                    
                    loanRepository.save(loan);
                }
                // Если недостаточно средств, можно добавить логику штрафов или уведомлений
            } catch (Exception e) {
                // Логирование ошибки
                System.err.println("Error processing monthly payment for loan ID: " + loan.getId());
                e.printStackTrace();
                // Продолжаем с другими кредитами
            }
        }
    }

    private void validateLoan(Loan loan) {
        if (loan.getAmount() <= 0) {
            throw new RuntimeException("Loan amount must be positive");
        }
        if (loan.getInterestRate() <= 0) {
            throw new RuntimeException("Interest rate must be positive");
        }
        if (loan.getTermInMonths() <= 0) {
            throw new RuntimeException("Loan term must be positive");
        }
        if (loan.getUser() == null) {
            throw new RuntimeException("User must be specified");
        }
    }

    // Метод для конвертации Loan в LoanDTO
    public LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setAmount(loan.getAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setTermInMonths(loan.getTermInMonths());
        dto.setStatus(loan.getStatus().getName());
        dto.setRemainingAmount(loan.getRemainingAmount());
        dto.setMonthlyPayment(loan.calculateMonthlyPayment());
        dto.setStartDate(loan.getStartDate());
        dto.setEndDate(loan.getEndDate());
        
        if (loan.getUser() != null) {
            dto.setUserId(loan.getUser().getId());
            dto.setUsername(loan.getUser().getUsername());
        }
        
        return dto;
    }
}