package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.RoleDto;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.services.RoleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/roles")
@CrossOrigin("*")
public class RoleController {
    @Autowired
    RoleServices roleServices;

    @PostMapping("/create")
    public ResponseEntity<?> createRole(@RequestBody RoleDto roleDTO) {
        try {
            RolesEntity role = roleServices.createRole(roleDTO.getRoleName(), roleDTO.getPermissions());
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping()
    public List<RolesEntity> findAll(){
       return roleServices.findAllRoles();
    }

}
