package org.example.bank.Service;

import org.example.bank.DTO.JWTResponse;
import org.example.bank.Entity.Account;
import org.example.bank.Entity.RefreshToken;
import org.example.bank.Entity.Role;
import org.example.bank.Entity.User;
import org.example.bank.Repository.AccountRepository;
import org.example.bank.Repository.RefreshTokenRepository;
import org.example.bank.Repository.RoleRepository;
import org.example.bank.Repository.UserRepository;
import org.example.bank.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepo;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    public AuthService(AuthenticationManager authenticationManager, UserService userService,
                      RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                      AccountRepository accountRepo, JwtUtil jwtUtil,
                      RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepo = accountRepo;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public User register(User user) {
        if (user.getRole() == null) {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRole(defaultRole);
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        User savedUser = userService.createUser(user);

        Account account = new Account();
        account.setUser(savedUser);
        account.setBalance(0.0);
        account = accountRepo.save(account);


        savedUser.setAccount(account);
        return userService.createUser(savedUser);
    }

    public Authentication authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }


    public String getRefreshToken(User user) {
        return refreshTokenRepository.findByUser (user)
                .map(RefreshToken::getToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    }

    public JWTResponse login(String username, String password) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Retrieve the user from the database
        User user = userService.getUserByUsername(username);
        String roleName = user.getRole() != null ? user.getRole().getName() : "ROLE_USER";

        // Generate a new access token
        String accessToken = jwtUtil.generateToken(username, roleName);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        // Check for existing refresh token
        RefreshToken existingToken = refreshTokenRepository.findByUser (user)
                .orElse(new RefreshToken());

        // Update or set the refresh token
        existingToken.setUser (user); // Ensure the user is set
        existingToken.setToken(refreshToken);
        existingToken.setExpiryDate(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)); // Set expiry to 7 days
        refreshTokenRepository.save(existingToken); // Save the refresh token

        // Return the JWT response containing both tokens
        return new JWTResponse(accessToken, refreshToken);
    }
}

