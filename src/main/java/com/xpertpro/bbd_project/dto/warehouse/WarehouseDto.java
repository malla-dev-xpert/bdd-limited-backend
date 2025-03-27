package com.xpertpro.bbd_project.dto.warehouse;

import com.xpertpro.bbd_project.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class WarehouseDto {

    private String name;
    private String adresse;
    private String storageType;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();

}
