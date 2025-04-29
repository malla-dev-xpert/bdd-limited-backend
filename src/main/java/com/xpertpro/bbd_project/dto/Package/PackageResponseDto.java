package com.xpertpro.bbd_project.dto.Package;

import com.xpertpro.bbd_project.dto.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class PackageResponseDto {
        private Long id;
        private String reference;
        private Double weight;
        private String dimensions;
        private LocalDateTime createdAt;
        private LocalDateTime editedAt;
        private Long warehouseId;
        private String warehouseName;
        private String warehouseAddress;
        private Long containerId;
        private Long userId;
        private Long partnerId;
        private String partnerName;
        private String partnerPhoneNumber;
        private String status;
        private List<ItemDto> items;
}
