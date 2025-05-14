package org.example.bank.Service;

import org.example.bank.Entity.Role;
import org.example.bank.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }



    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void deleteRole(String name) {
        Role role = getRoleByName(name);
        if (role.getName().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Cannot delete ADMIN role");
        }
        roleRepository.delete(role);
    }

    public Role updateRole(Long id, String newName) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        if (role.getName().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Cannot modify ADMIN role");
        }
        role.setName(newName);
        return roleRepository.save(role);
    }
} 