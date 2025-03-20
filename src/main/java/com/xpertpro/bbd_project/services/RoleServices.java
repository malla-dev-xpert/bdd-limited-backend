package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.enums.PermissionsEnum;
import com.xpertpro.bbd_project.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RoleServices {
    @Autowired
    private RoleRepository roleRepository;

    public RolesEntity createRole(String roleName, Set<PermissionsEnum> permissions) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new IllegalArgumentException("Ce rôle existe déjà !");
        }
        RolesEntity role = new RolesEntity();
        role.setName(roleName);
        role.setPermissions(permissions);  // Affectation des permissions sous forme d'énumération
        return roleRepository.save(role);
    }

    public List<RolesEntity> findAllRoles(){
        return roleRepository.findAll();
    }
}
