package com.xpertpro.bbd_project.dto.partners;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class UpdatePartnersDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String country;
    private String adresse;
    private String accountType;
    private LocalDateTime editedAt = LocalDateTime.now();
}
