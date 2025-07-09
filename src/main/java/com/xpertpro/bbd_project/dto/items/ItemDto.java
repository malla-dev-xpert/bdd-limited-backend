package com.xpertpro.bbd_project.dto.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class ItemDto {
    private Long id;
    private String description;
    private String supplierName;
    private String supplierPhone;
    private Double totalPrice;
    private Integer quantity;
    private Long packageId;
    private Double unitPrice;
    private Double salesRate;
    private String status;
    private Long clientId;
    private String invoiceNumber;
    private Long supplierId;
    private List<Long> itemIds;
}
