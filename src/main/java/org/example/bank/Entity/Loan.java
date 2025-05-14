package org.example.bank.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    private Double interestRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double remainingAmount;
    private Integer termInMonths;
    
    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private LoanStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(Double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Integer getTermInMonths() {
        return termInMonths;
    }

    public void setTermInMonths(Integer termInMonths) {
        this.termInMonths = termInMonths;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double calculateMonthlyPayment() {
        if (termInMonths == null || termInMonths <= 0) {
            return 0.0;
        }
        double monthlyRate = interestRate / 12 / 100;
        return (amount * monthlyRate * Math.pow(1 + monthlyRate, termInMonths)) 
               / (Math.pow(1 + monthlyRate, termInMonths) - 1);
    }

    public Double calculateTotalAmount() {
        return calculateMonthlyPayment() * termInMonths;
    }
}
