package org.example.bank.Controller;

import org.example.bank.Entity.User;
import org.example.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Получение списка всех пользователей
    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Получение информации о пользователе по ID
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Создание нового пользователя
    @PostMapping("/user")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Обновление данных пользователя
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // Обновление роли пользователя
    @PutMapping("/user/{id}/role/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User updateUserRole(@PathVariable Long id, @PathVariable String roleName) {
        return userService.updateUserRole(id, roleName);
    }

    // Удаление пользователя
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // Получение списка VIP пользователей
    @GetMapping("/vip-users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getVipUsers() {
        return userService.getVipUsers();
    }

    // Получение списка заблокированных пользователей
    @GetMapping("/blocked-users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getBlockedUsers() {
        return userService.getBlockedUsers();
    }

    // Получение пользователей по роли
    @GetMapping("/users-by-role/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getUsersByRole(@PathVariable String roleName) {
        return userService.getUsersByRole(roleName);
    }
}
