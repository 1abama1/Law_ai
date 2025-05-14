package org.example.bank.Controller;

import org.example.bank.DTO.UserDTO;
import org.example.bank.Entity.User;
import org.example.bank.Entity.UserBlock;
import org.example.bank.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public List<UserDTO> getAllUsers(@RequestParam(required = false) String username) {
        if (username != null && !username.isEmpty()) {
            return userService.findUsersByUsernameDTO(username);
        }
        return userService.getAllUsersDTO();
    }

    // Получение информации о пользователе по ID
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserByIdDTO(id);
    }

    // Создание нового пользователя
    @PostMapping("/user")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserDTO createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return userService.convertToDTO(createdUser);
    }

    // Обновление данных пользователя
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return userService.convertToDTO(updatedUser);
    }

    // Обновление роли пользователя
    @PutMapping("/user/{id}/role/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserDTO updateUserRole(@PathVariable Long id, @PathVariable String roleName) {
        User updatedUser = userService.updateUserRole(id, roleName);
        return userService.convertToDTO(updatedUser);
    }

    // Удаление пользователя
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
        userService.deleteUser(id);
            return ResponseEntity.ok("User successfully deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error deleting user: " + e.getMessage());
        }
    }

    // Получение списка VIP пользователей
    @GetMapping("/vip-users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserDTO> getVipUsers() {
        return userService.getUsersByRoleDTO("ROLE_VIP");
    }

    // Получение списка заблокированных пользователей
    @GetMapping("/blocked-users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserDTO> getBlockedUsers() {
        return userService.getUsersByRoleDTO("ROLE_BLOCKED");
    }

    // Получение пользователей по роли
    @GetMapping("/users-by-role/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserDTO> getUsersByRole(@PathVariable String roleName) {
        return userService.getUsersByRoleDTO(roleName);
    }
    
    // Блокировка пользователя на время
    @PostMapping("/user/{id}/block/temporary")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> blockUserTemporary(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> blockInfo) {
        
        try {
            Integer days = (Integer) blockInfo.get("days");
            String reason = (String) blockInfo.get("reason");
            
            if (days == null) {
                return ResponseEntity.badRequest().body("Days parameter is required");
            }
            
            UserBlock block = userService.blockUserTemporary(id, days, reason);
            return ResponseEntity.ok("User blocked successfully for " + days + " days");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error blocking user: " + e.getMessage());
        }
    }
    
    // Блокировка пользователя навсегда
    @PostMapping("/user/{id}/block/permanent")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> blockUserPermanent(
            @PathVariable Long id,
            @RequestBody Map<String, String> blockInfo) {
        
        try {
            String reason = blockInfo.get("reason");
            UserBlock block = userService.blockUserPermanent(id, reason);
            return ResponseEntity.ok("User blocked permanently");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error blocking user: " + e.getMessage());
        }
    }
    
    // Разблокировка пользователя
    @PostMapping("/user/{id}/unblock")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        try {
            User user = userService.unblockUser(id);
            return ResponseEntity.ok("User unblocked successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error unblocking user: " + e.getMessage());
        }
    }
}
