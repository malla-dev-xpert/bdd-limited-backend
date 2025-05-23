package com.xpertpro.bbd_project.dto.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ItemDto {
    private Long id;
    private String description;
    private double quantity;
    private double unitPrice;
}
