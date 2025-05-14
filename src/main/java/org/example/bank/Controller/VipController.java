package org.example.bank.Controller;

import org.example.bank.Entity.User;
import org.example.bank.Security.JwtUtil;
import org.example.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vip")
public class VipController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public VipController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // Получение данных текущего VIP пользователя
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public User getCurrentVip(@RequestHeader("Authorization") String token) {
        String username = extractUsernameFromToken(token);
        return userService.getUserByUsername(username);
    }

    // Обновление данных VIP пользователя
    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public User updateVip(@RequestHeader("Authorization") String token, @RequestBody User user) {
        String username = extractUsernameFromToken(token);
        User existingUser = userService.getUserByUsername(username);
        user.setId(existingUser.getId());
        return userService.updateUser(existingUser.getId(), user);
    }

    // Получение списка других VIP пользователей
    @GetMapping("/other-vips")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public List<User> getOtherVips(@RequestHeader("Authorization") String token) {
        String currentUsername = extractUsernameFromToken(token);
        return userService.getVipUsers().stream()
                .filter(user -> !user.getUsername().equals(currentUsername))
                .toList();
    }

    // Обновление VIP статуса (только для администратора)
    @PutMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User updateVipStatus(@RequestBody User user) {
        return userService.updateUser(user.getId(), user);
    }

    // Получение информации о VIP пользователе по ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public User getVipById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (!user.getRole().getName().equals("ROLE_VIP")) {
            throw new RuntimeException("User is not a VIP");
        }
        return user;
    }

    private String extractUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token.substring(7));
    }
}
