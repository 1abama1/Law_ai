package org.example.bank.Controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.bank.DTO.TransactionDto;
import org.example.bank.Entity.Account;
import org.example.bank.Entity.Transaction;
import org.example.bank.Entity.User;
import org.example.bank.Repository.AccountRepository;
import org.example.bank.Repository.RefreshTokenRepository;
import org.example.bank.Repository.TransactionRepository;
import org.example.bank.Repository.UserRepository;
import org.example.bank.Security.JwtUtil;
import org.example.bank.Service.RefreshTokenService;
import org.example.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    @Autowired
    private TransactionRepository transRepo;
    @Autowired
    private AccountRepository accRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshTokenRepo;
    @Autowired
    private RefreshTokenService refreshTokenService;


    @GetMapping("/my")
    public List<TransactionDto> myTransactions(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long userId = refreshTokenService.getUserIdByRefreshToken(token);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User  not found"));
        Account acc = accRepo.findByUser (user)
                .orElseThrow(() -> new RuntimeException("Account not found for user"));

        List<Transaction> transactions = transRepo.findByFromAccountOrToAccount(acc, acc);

        return transactions.stream().map(t -> {
            TransactionDto dto = new TransactionDto();
            dto.amount = t.getAmount();
            dto.timestamp = t.getTimestamp();
            dto.fromAccount = t.getFromAccount().getId();
            dto.toAccount = t.getToAccount().getId();
            dto.direction = t.getFromAccount().getId().equals(acc.getId()) ? "OUT" : "IN";

            dto.fromUsername = t.getFromAccount().getUser().getUsername();
            dto.toUsername = t.getToAccount().getUser().getUsername();

            return dto;
        }).collect(Collectors.toList());
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}
