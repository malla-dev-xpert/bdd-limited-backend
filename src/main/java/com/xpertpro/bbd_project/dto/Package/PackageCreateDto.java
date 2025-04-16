package com.xpertpro.bbd_project.dto.Package;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageCreateDto {
    private String reference;
    private Double weight;
    private String dimensions;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();
}
