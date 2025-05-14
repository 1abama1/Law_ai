package org.example.bank.Repository;

import org.example.bank.Entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface LoanStatusRepository extends JpaRepository<LoanStatus, Long> {
    LoanStatus findByName(String name);
} 