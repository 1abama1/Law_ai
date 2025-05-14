package org.example.bank.Controller;

import org.example.bank.DTO.LoanRequestDTO;
import org.example.bank.Entity.Loan;
import org.example.bank.Entity.User;
import org.example.bank.Service.LoanService;
import org.example.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody LoanRequestDTO loanRequest) {
        User user = userService.getUserById(loanRequest.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Loan loan = new Loan();
        loan.setAmount(loanRequest.getAmount());
        loan.setInterestRate(loanRequest.getInterestRate());
        loan.setUser(user);
        loan.setTermInMonths(12); // По умолчанию 12 месяцев, можно изменить

        return ResponseEntity.ok(loanService.createLoan(loan));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.approveLoan(loanId));
    }

    @PostMapping("/{loanId}/payment")
    public ResponseEntity<Loan> makePayment(
            @PathVariable Long loanId,
            @RequestParam Double amount) {
        return ResponseEntity.ok(loanService.makePayment(loanId, amount));
    }
}