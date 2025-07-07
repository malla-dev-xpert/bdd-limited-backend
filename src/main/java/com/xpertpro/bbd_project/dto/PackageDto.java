package com.xpertpro.bbd_project.dto;

import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.items.ItemDto;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class PackageDto {
    private Long id;
    private String expeditionType;
    private double weight;
    private double cbn;
    private double itemQuantity;
    private String startCountry;
    private String ref;
    private String destinationCountry;
    private LocalDateTime arrivalDate;
    private LocalDateTime startDate;
    private Long clientId;
    private Long warehouseId;
    private String clientName;
    private String clientPhone;
    private String status;
    private String warehouseName;
    private String warehouseAddress;
    private List<Long> itemIds;
    private List<ItemDto> items;
}
