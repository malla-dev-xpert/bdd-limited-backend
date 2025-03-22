package com.xpertpro.bbd_project.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String username;
    private LocalDateTime editedAt = LocalDateTime.now();
}
