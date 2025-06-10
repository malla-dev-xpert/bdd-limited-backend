package com.xpertpro.bbd_project.dto.achats;

import lombok.Data;

@Data
public class CreateItemsDto {
    private String description;
    private Integer quantity;
    private double unitPrice;
}
