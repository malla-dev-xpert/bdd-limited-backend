package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.Roles;
import com.xpertpro.bbd_project.enums.Permissions;
import com.xpertpro.bbd_project.enums.RoleEnum;
import com.xpertpro.bbd_project.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServices {
    @Autowired
    private RoleRepository roleRepository;

    public Roles addPermissionToRole(RoleEnum roleName, Permissions permission) {
        Roles role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("ROLE_NOT_FOUND"));

        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }
}