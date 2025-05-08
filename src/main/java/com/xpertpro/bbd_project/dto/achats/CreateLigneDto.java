package com.xpertpro.bbd_project.dto.achats;

import lombok.Data;

@Data
public class CreateLigneDto {
    private String descriptionItem;
    private Integer quantityItem;
    private double prixUnitaire;
}
