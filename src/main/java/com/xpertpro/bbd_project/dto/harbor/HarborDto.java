package com.xpertpro.bbd_project.dto.harbor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class HarborDto {
    private String name;
    private String location;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();
}
