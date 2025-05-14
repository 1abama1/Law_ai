package org.example.bank.Controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.bank.DTO.JWTResponse;
import org.example.bank.DTO.LoginRequest;
import org.example.bank.DTO.RegisterRequest;
import org.example.bank.Entity.Account;
import org.example.bank.Entity.Role;
import org.example.bank.Entity.RefreshToken;
import org.example.bank.Entity.User;
import org.example.bank.Repository.AccountRepository;
import org.example.bank.Repository.RefreshTokenRepository;
import org.example.bank.Repository.RoleRepository;
import org.example.bank.Repository.UserRepository;
import org.example.bank.Security.JwtUtil;
import org.example.bank.Service.AuthService;
import org.example.bank.Service.RefreshTokenService;
import org.example.bank.Service.UserService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private RefreshTokenService refreshTokenService;


    @Autowired
    public AuthController(AuthService authService, RoleRepository roleRepository) {
        this.authService = authService;
        this.roleRepository = roleRepository;
    }

    public String getRefreshToken(User user) {
        return refreshTokenRepository.findByUser (user)
                .map(RefreshToken::getToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/refresh-token/{userId}")
    public ResponseEntity<JWTResponse> refreshToken(@PathVariable Long userId) {
        String refreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Refresh token not found for user ID: " + userId)).getToken();

        String username = jwtUtil.extractUsername(refreshToken);
        User user = userService.getUserByUsername(username);

        String newAccessToken = jwtUtil.generateToken(username, user.getRole().getName());
        return ResponseEntity.ok(new JWTResponse(newAccessToken, refreshToken));
    }


    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> balance(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        String username = jwtUtil.extractUsername(token);

        // Log the username for debugging
        System.out.println("Authenticated user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User  not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("balance", user.getAccount().getBalance());

        Account acc = accountRepository.findByUser (user)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return ResponseEntity.ok(response);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }

    @PostMapping("/validate-session")
    public ResponseEntity<?> validateSession(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            String username = jwtUtil.extractUsername(token);

            if (!jwtUtil.validate(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            HashMap<String, Object> response = new HashMap<>();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().getName());
            response.put("balance", user.getAccount().getBalance());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRole(defaultRole);
        }

        authService.register(user);
        return ResponseEntity.ok("User  registered successfully");
    }



    @PostMapping("/login")
    public ResponseEntity<JWTResponse> login(@RequestBody LoginRequest request) {
        JWTResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
