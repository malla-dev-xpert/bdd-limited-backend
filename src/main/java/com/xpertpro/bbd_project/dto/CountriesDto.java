package com.xpertpro.bbd_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class CountriesDto {
    private String name;
    private String isoCode;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();
}
