package com.xpertpro.bbd_project.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
