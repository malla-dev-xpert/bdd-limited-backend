package com.xpertpro.bbd_project.dto;

import com.xpertpro.bbd_project.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String photoUrl;
    private String username;
    private RoleEnum userRoleEnum;
}
