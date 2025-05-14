package org.example.bank.Controller;

import org.example.bank.Entity.Role;
import org.example.bank.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String getUser() {
        return "Hello, User!";
    }

    @GetMapping("/vip")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public String getVip() {
        return "Hello, VIP User!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getAdmin() {
        return "Hello, Admin!";
    }

    @GetMapping("/user/role/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserRole(@PathVariable String username) {
        return userRepository.findRoleByUsername(username)
                .map(role -> ResponseEntity.ok(role))
                .orElse(ResponseEntity.notFound().build());
    }
}
