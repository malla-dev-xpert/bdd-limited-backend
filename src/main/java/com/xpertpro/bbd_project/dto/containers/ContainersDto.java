package com.xpertpro.bbd_project.dto.containers;

import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class ContainersDto {
    private Long id;
    private String reference;
    private String size;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();
    private Boolean isAvailable;
    private String status;
    private Long userId;
    private String userName;
    private Long harborId;
    private String harborName;
    private List<PackageResponseDto> packages;
}
