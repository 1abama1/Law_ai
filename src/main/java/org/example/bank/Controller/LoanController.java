package org.example.bank.Controller;

import org.example.bank.DTO.LoanDTO;
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

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return ResponseEntity.ok(loanService.getLoansByStatusDTO(status));
        }
        return ResponseEntity.ok(loanService.getAllLoansDTO());
    }

    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@RequestBody LoanRequestDTO loanRequest) {
        User user = userService.getUserById(loanRequest.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Loan loan = new Loan();
        loan.setAmount(loanRequest.getAmount());
        loan.setInterestRate(loanRequest.getInterestRate());
        loan.setUser(user);
        loan.setTermInMonths(loanRequest.getTermInMonths() != null ? loanRequest.getTermInMonths() : 12);

        Loan createdLoan = loanService.createLoan(loan);
        return ResponseEntity.ok(loanService.convertToDTO(createdLoan));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanDTO>> getLoansByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUserIdDTO(userId));
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<LoanDTO> approveLoan(@PathVariable Long loanId) {
        Loan approvedLoan = loanService.approveLoan(loanId);
        return ResponseEntity.ok(loanService.convertToDTO(approvedLoan));
    }

    @PostMapping("/{loanId}/payment")
    public ResponseEntity<LoanDTO> makePayment(
            @PathVariable Long loanId,
            @RequestParam Double amount) {
        Loan updatedLoan = loanService.makePayment(loanId, amount);
        return ResponseEntity.ok(loanService.convertToDTO(updatedLoan));
    }
}