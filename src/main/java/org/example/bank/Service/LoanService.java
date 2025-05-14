package org.example.bank.Service;

import org.example.bank.Entity.Loan;
import org.example.bank.Entity.LoanStatus;
import org.example.bank.Repository.LoanRepository;
import org.example.bank.Repository.LoanStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanStatusRepository loanStatusRepository;

    @Transactional
    public Loan createLoan(Loan loan) {
        validateLoan(loan);
        LoanStatus pendingStatus = loanStatusRepository.findByName("PENDING");
        if (pendingStatus == null) {
            throw new RuntimeException("Loan status 'PENDING' not found in database");
        }
        loan.setStatus(pendingStatus);
        loan.setStartDate(LocalDateTime.now());
        loan.setEndDate(loan.getStartDate().plusMonths(loan.getTermInMonths()));
        loan.setRemainingAmount(loan.calculateTotalAmount());
        return loanRepository.save(loan);
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Transactional
    public Loan approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        LoanStatus activeStatus = loanStatusRepository.findByName("ACTIVE");
        if (activeStatus == null) {
            throw new RuntimeException("Loan status 'ACTIVE' not found in database");
        }
        loan.setStatus(activeStatus);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan makePayment(Long loanId, Double paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        
        LoanStatus activeStatus = loanStatusRepository.findByName("ACTIVE");
        if (loan.getStatus().getId() != activeStatus.getId()) {
            throw new RuntimeException("Loan is not active");
        }

        if (paymentAmount > loan.getRemainingAmount()) {
            throw new RuntimeException("Payment amount exceeds remaining loan amount");
        }

        loan.setRemainingAmount(loan.getRemainingAmount() - paymentAmount);
        
        if (loan.getRemainingAmount() <= 0) {
            LoanStatus paidStatus = loanStatusRepository.findByName("PAID");
            if (paidStatus == null) {
                throw new RuntimeException("Loan status 'PAID' not found in database");
            }
            loan.setStatus(paidStatus);
        }
        
        return loanRepository.save(loan);
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
}