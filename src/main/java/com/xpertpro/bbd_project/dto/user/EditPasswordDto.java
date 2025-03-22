package com.xpertpro.bbd_project.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class EditPasswordDto {
    private String oldPassword;
    private String newPassword;
    private LocalDateTime editedAt = LocalDateTime.now();
}
