package org.example.bank.Controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.bank.DTO.TransactionDTO;
import org.example.bank.Entity.Account;
import org.example.bank.Entity.Transaction;
import org.example.bank.Entity.User;
import org.example.bank.Repository.AccountRepository;
import org.example.bank.Repository.RefreshTokenRepository;
import org.example.bank.Repository.TransactionRepository;
import org.example.bank.Repository.UserRepository;
import org.example.bank.Security.JwtUtil;
import org.example.bank.Service.RefreshTokenService;
import org.example.bank.Service.TransactionService;
import org.example.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/my")
    public List<TransactionDTO> myTransactions(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long userId = refreshTokenService.getUserIdByRefreshToken(token);

        // Получаем все транзакции пользователя (кредитные + переводы)
        List<TransactionDTO> allTransactions = new ArrayList<>();
        
        // Добавляем транзакции по кредитам
        allTransactions.addAll(transactionService.getUserTransactionsDTO(userId));
        
        // Добавляем транзакции по переводам между пользователями
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account acc = accRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account not found for user"));

        // Создаем записи типа TransactionDTO для транзакций между счетами
        // Это временное решение до полной миграции на новую модель транзакций
        List<Transaction> accountTransactions = transRepo.findByFromAccountOrToAccount(acc, acc);
        
        for (Transaction t : accountTransactions) {
            TransactionDTO dto = new TransactionDTO();
            dto.setId(t.getId());
            dto.setAmount(t.getAmount());
            dto.setDate(t.getDate());
            
            if (t.getFromAccount().getId().equals(acc.getId())) {
                dto.setType("TRANSFER_OUT");
                dto.setDescription("Перевод на счет пользователя " + t.getToAccount().getUser().getUsername());
            } else {
                dto.setType("TRANSFER_IN");
                dto.setDescription("Получен перевод от пользователя " + t.getFromAccount().getUser().getUsername());
            }
            
            dto.setUserId(userId);
            dto.setUsername(user.getUsername());
            
            allTransactions.add(dto);
        }
        
        // Сортируем по дате (от новых к старым)
        return allTransactions.stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                .collect(Collectors.toList());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getUserTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getUserTransactionsDTO(userId));
    }
    
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<TransactionDTO>> getLoanTransactions(@PathVariable Long loanId) {
        return ResponseEntity.ok(transactionService.getLoanTransactionsDTO(loanId));
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}
