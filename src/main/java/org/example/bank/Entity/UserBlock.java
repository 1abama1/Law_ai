package org.example.bank.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_blocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime blockDate;

    @Column
    private LocalDateTime expiryDate; // null means permanent block

    @Column(length = 500)
    private String reason;

    public boolean isPermanent() {
        return expiryDate == null;
    }

    public boolean isActive() {
        return expiryDate == null || LocalDateTime.now().isBefore(expiryDate);
    }
} 