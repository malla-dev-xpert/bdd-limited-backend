package com.xpertpro.bbd_project.dto.achats;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CreateLigneDto {
    private String descriptionItem;
    private Integer quantityItem;
    private double prixUnitaire;
    @NotNull
    private Long supplierId;
}
