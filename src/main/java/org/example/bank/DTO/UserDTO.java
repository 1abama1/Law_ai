package org.example.bank.DTO;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String roleName;
    private Double balance;
    private Boolean isBlocked;
    private LocalDateTime blockExpiryDate;
    private String blockReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
    
    public Boolean getIsBlocked() {
        return isBlocked;
    }
    
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
    
    public LocalDateTime getBlockExpiryDate() {
        return blockExpiryDate;
    }
    
    public void setBlockExpiryDate(LocalDateTime blockExpiryDate) {
        this.blockExpiryDate = blockExpiryDate;
    }
    
    public String getBlockReason() {
        return blockReason;
    }
    
    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }
} 