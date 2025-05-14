package org.example.bank.Controller;

import org.example.bank.Entity.Role;
import org.example.bank.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

//    @PostMapping
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public Role createRole(@RequestBody String roleName) {
//        return roleService.createRole(roleName);
//    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Role getRoleByName(@PathVariable String name) {
        return roleService.getRoleByName(name);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Role updateRole(@PathVariable Long id, @RequestBody String newName) {
        return roleService.updateRole(id, newName);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteRole(@PathVariable String name) {
        roleService.deleteRole(name);
    }
} 