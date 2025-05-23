package com.xpertpro.bbd_project.dto.user;

import com.xpertpro.bbd_project.entity.RolesEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class findUserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String username;
    private String status;
    private RolesEntity role;
}
