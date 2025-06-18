package com.xpertpro.bbd_project.dto.items;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemResponseDto {
    private Long itemId;
    private String description;
    private double quantity;
    private double unitPrice;
    private LocalDateTime achatDate;
    private Long clientId;
    private String clientName;
    private String clientPhone;
    private Long supplierId;
    private String supplierName;
    private String supplierPhone;
    private String status;
}
