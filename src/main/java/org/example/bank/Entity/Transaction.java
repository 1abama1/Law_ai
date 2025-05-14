package org.example.bank.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;


import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;
    private Double amount;
    private LocalDateTime timestamp;

    @ManyToOne
    private Account fromAccount;
    @ManyToOne
    private Account toAccount;

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTimestamp(LocalDateTime now) {
        this.timestamp = now;
    }

    public void setFromAccount(Account from) {
        this.fromAccount = from;
    }

    public void setToAccount(Account to) {
        this.toAccount = to;
    }

    public Double getAmount() {
        return amount;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }
}