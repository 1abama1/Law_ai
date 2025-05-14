package org.example.bank.Controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement; import org.example.bank.DTO.TransferRequest;
import org.example.bank.DTO.TransferResponse;
import org.example.bank.Entity.Account; import org.example.bank.Entity.Transaction; import org.example.bank.Entity.User; import org.example.bank.Repository.AccountRepository; import org.example.bank.Repository.RefreshTokenRepository; import org.example.bank.Repository.TransactionRepository; import org.example.bank.Repository.UserRepository; import org.example.bank.Security.JwtUtil;
import org.example.bank.Service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;

import java.security.Principal; import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AccountRepository accRepo;
    @Autowired
    private TransactionRepository transRepo;
    @Autowired
    private RefreshTokenRepository refreshTokenRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TransferRequest req
    ) {
        try {
            String token = extractToken(authHeader);
            Long userId = refreshTokenService.getUserIdByRefreshToken(token);

            User sender = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Account from = accRepo.findByUser(sender)
                    .orElseThrow(() -> new RuntimeException("Account not found for user"));

            Account to = accRepo.findById(req.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("Recipient account not found"));

            if (from.getBalance() < req.getAmount()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Недостаточно средств", "currentBalance", from.getBalance())
                );
            }

            // Сохраняем старый баланс для расчета нового
            Double senderOldBalance = from.getBalance();

            from.setBalance(senderOldBalance - req.getAmount());
            to.setBalance(to.getBalance() + req.getAmount());

            Transaction transaction = new Transaction();
            transaction.setAmount(req.getAmount());
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setFromAccount(from);
            transaction.setToAccount(to);

            transRepo.save(transaction);
            Account savedFromAccount = accRepo.save(from);
            accRepo.save(to);

            TransferResponse response = new TransferResponse(       );
            response.setMessage("Перевод успешно выполнен");
            response.setAmount(req.getAmount());
            response.setFromAccountId(savedFromAccount.getId());
            response.setToAccountId(to.getId());
            response.setSenderNewBalance(savedFromAccount.getBalance());
            response.setTimestamp(LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Ошибка перевода",
                            "details", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Double> balance(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long userId = refreshTokenService.getUserIdByRefreshToken(token);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account acc = accRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Account not found for user"));

        return ResponseEntity.ok(acc.getBalance());
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else {
            throw new RuntimeException("Invalid authorization header");
        }
    }

}