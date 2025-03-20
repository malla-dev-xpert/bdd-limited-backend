package com.xpertpro.bbd_project.dto;

import com.xpertpro.bbd_project.enums.PermissionsEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data @AllArgsConstructor @NoArgsConstructor
public class RoleDto {
    private String roleName;
    private Set<PermissionsEnum> permissions;
}
