package com.xpertpro.bbd_project.dto.achats;

import lombok.Data;

@Data
public class CreateItemsDto {
    private String description;
    private Integer quantity;
    private double unitPrice;
    private double salesRate;
    private Long supplierId;
    private String invoiceNumber;
}
