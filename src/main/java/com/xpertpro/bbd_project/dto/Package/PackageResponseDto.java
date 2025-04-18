package com.xpertpro.bbd_project.dto.Package;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class PackageResponseDto {
        private Long id;
        private String reference;
        private Double weight;
        private String dimensions;
        private LocalDateTime createdAt;
        private LocalDateTime editedAt;
        private Long warehouseId;
        private Long containerId;
        private Long userId;
        private Long partnerId;
        private String partnerName;
        private String partnerPhoneNumber;
        private String status;
}
