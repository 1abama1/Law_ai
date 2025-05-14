package org.example.bank.Service;

import org.example.bank.Entity.Role;
import org.example.bank.Entity.User;
import org.example.bank.Repository.RoleRepository;
import org.example.bank.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (user.getRole().getName().equals("ROLE_ADMIN") && 
            userRepository.countByRole_Name("ROLE_ADMIN") <= 1) {
            throw new RuntimeException("Cannot delete the last admin");
        }
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
}
