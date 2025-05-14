package org.example.bank.Service;

import org.example.bank.DTO.UserDTO;
import org.example.bank.Entity.Role;
import org.example.bank.Entity.User;
import org.example.bank.Entity.UserBlock;
import org.example.bank.Repository.RoleRepository;
import org.example.bank.Repository.UserBlockRepository;
import org.example.bank.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserBlockRepository userBlockRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, 
                      UserBlockRepository userBlockRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userBlockRepository = userBlockRepository;
        this.passwordEncoder = passwordEncoder;
        initializeDefaultRoles();
    }

    private void initializeDefaultRoles() {
        if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }   
        if (!roleRepository.findByName("ROLE_VIP").isPresent()) {
            roleRepository.save(new Role("ROLE_VIP"));
        }
        if (!roleRepository.findByName("ROLE_USER").isPresent()) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (!roleRepository.findByName("ROLE_BLOCKED").isPresent()) {
            roleRepository.save(new Role("ROLE_BLOCKED"));
        }
    }

    // Конвертировать User в UserDTO
    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }
        
        if (user.getAccount() != null) {
            dto.setBalance(user.getAccount().getBalance());
        }
        
        // Set blocking information
        boolean isBlocked = user.getRole() != null && user.getRole().getName().equals("ROLE_BLOCKED");
        dto.setIsBlocked(isBlocked);
        
        if (isBlocked) {
            userBlockRepository.findByUserId(user.getId()).ifPresent(block -> {
                dto.setBlockExpiryDate(block.getExpiryDate());
                dto.setBlockReason(block.getReason());
            });
        }
        
        return dto;
    }
    
    // Получить все DTO пользователей
    public List<UserDTO> getAllUsersDTO() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Найти пользователей по имени и вернуть как DTO
    public List<UserDTO> findUsersByUsernameDTO(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Получить конкретного пользователя как DTO по ID
    public UserDTO getUserByIdDTO(Long id) {
        User user = getUserById(id);
        return convertToDTO(user);
    }
    
    // Получить пользователей по роли как DTO
    public List<UserDTO> getUsersByRoleDTO(String roleName) {
        return userRepository.findByRole_Name(roleName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public User createUser(User user) {
        if (user.getRole() == null) {
            user.setRole(roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found")));
        }
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        existingUser.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        return userRepository.save(existingUser);
    }

    @Transactional
    public User updateUserRole(Long id, String roleName) {
        User user = getUserById(id);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if (user.getRole().getName().equals("ROLE_ADMIN") && 
            userRepository.countByRole_Name("ROLE_ADMIN") <= 1) {
            throw new RuntimeException("Cannot change role of the last admin");
        }
        
        user.setRole(role);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public List<User> findUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (user.getRole().getName().equals("ROLE_ADMIN") && 
            userRepository.countByRole_Name("ROLE_ADMIN") <= 1) {
            throw new RuntimeException("Cannot delete the last admin");
        }
        
        // Delete any block records associated with this user
        userBlockRepository.findByUserId(id).ifPresent(block -> userBlockRepository.delete(block));
        
        // Delete the user
        userRepository.deleteById(id);
    }

    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRole_Name(roleName);
    }

    public List<User> getVipUsers() {
        return userRepository.findByRole_Name("ROLE_VIP");
    }

    public List<User> getBlockedUsers() {
        return userRepository.findByRole_Name("ROLE_BLOCKED");
    }
    
    // Block a user temporarily
    @Transactional
    public UserBlock blockUserTemporary(Long userId, int days, String reason) {
        User user = getUserById(userId);
        
        // Check if this is the last admin
        if (user.getRole().getName().equals("ROLE_ADMIN") && 
            userRepository.countByRole_Name("ROLE_ADMIN") <= 1) {
            throw new RuntimeException("Cannot block the last admin");
        }
        
        // Store user's original role for later restoration
        String originalRole = user.getRole().getName();
        
        // Change user role to BLOCKED
        Role blockedRole = roleRepository.findByName("ROLE_BLOCKED")
                .orElseThrow(() -> new RuntimeException("ROLE_BLOCKED not found"));
        user.setRole(blockedRole);
        userRepository.save(user);
        
        // Create block record
        UserBlock block = new UserBlock();
        block.setUser(user);
        block.setBlockDate(LocalDateTime.now());
        block.setExpiryDate(LocalDateTime.now().plusDays(days));
        block.setReason(reason);
        
        return userBlockRepository.save(block);
    }
    
    // Block a user permanently
    @Transactional
    public UserBlock blockUserPermanent(Long userId, String reason) {
        User user = getUserById(userId);
        
        // Check if this is the last admin
        if (user.getRole().getName().equals("ROLE_ADMIN") && 
            userRepository.countByRole_Name("ROLE_ADMIN") <= 1) {
            throw new RuntimeException("Cannot block the last admin");
        }
        
        // Change user role to BLOCKED
        Role blockedRole = roleRepository.findByName("ROLE_BLOCKED")
                .orElseThrow(() -> new RuntimeException("ROLE_BLOCKED not found"));
        user.setRole(blockedRole);
        userRepository.save(user);
        
        // Create block record with no expiry date (permanent block)
        UserBlock block = new UserBlock();
        block.setUser(user);
        block.setBlockDate(LocalDateTime.now());
        block.setExpiryDate(null); // null means permanent
        block.setReason(reason);
        
        return userBlockRepository.save(block);
    }
    
    // Unblock a user
    @Transactional
    public User unblockUser(Long userId) {
        User user = getUserById(userId);
        
        // Only unblock if the user is currently blocked
        if (!user.getRole().getName().equals("ROLE_BLOCKED")) {
            throw new RuntimeException("User is not blocked");
        }
        
        // Get the block record to determine original role
        UserBlock block = userBlockRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User block record not found"));
        
        // Default to ROLE_USER if we can't determine original role
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRole(defaultRole);
        userRepository.save(user);
        
        // Delete the block record
        userBlockRepository.delete(block);
        
        return user;
    }
    
    // Check for and process expired blocks
    @Transactional
    public void processExpiredBlocks() {
        LocalDateTime now = LocalDateTime.now();
        List<UserBlock> expiredBlocks = userBlockRepository.findByExpiryDateBefore(now);
        
        for (UserBlock block : expiredBlocks) {
            // Skip permanent blocks
            if (block.getExpiryDate() == null) {
                continue;
            }
            
            // Unblock the user
            User user = block.getUser();
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.setRole(defaultRole);
            userRepository.save(user);
            
            // Delete the block record
            userBlockRepository.delete(block);
        }
    }
}
