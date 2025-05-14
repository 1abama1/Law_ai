package org.example.bank.Repository;

import org.example.bank.Entity.User;
import org.example.bank.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole_Name(String roleName);
    long countByRole_Name(String roleName);
    
    @Query("SELECT u.role FROM User u WHERE u.username = :username")
    Optional<Role> findRoleByUsername(String username);
}
