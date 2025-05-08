package com.xpertpro.bbd_project.dto.achats;

import lombok.Data;

@Data
public class LigneAchatDto {
    private Long id;
    private Long achatId;
    private Long itemId;
    private String descriptionItem;
    private double quantityItem;
    private double unitPriceItem;
    private Integer quantity;
    private Double prixTotal;
}
