package org.example.bank.DTO;

import java.time.LocalDateTime;

public class TransferResponse {
    private String message;
    private Double amount;
    private Long fromAccountId;
    private Long toAccountId;
    private Double senderNewBalance;
    private LocalDateTime timestamp;



    public TransferResponse() {

    }

    public TransferResponse(String message, Double amount, Long fromAccountId, Long toAccountId, Double senderNewBalance, LocalDateTime timestamp) {
        this.message = message;
        this.amount = amount;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.senderNewBalance = senderNewBalance;
        this.timestamp = timestamp;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Double getSenderNewBalance() {
        return senderNewBalance;
    }

    public void setSenderNewBalance(Double senderNewBalance) {
        this.senderNewBalance = senderNewBalance;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


}
