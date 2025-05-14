package org.example.bank.Repository;

import org.example.bank.Entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByStatusId(Long statusId);
    
    @Query("SELECT l FROM Loan l JOIN l.status s WHERE s.name = :statusName")
    List<Loan> findByStatusName(@Param("statusName") String statusName);
}