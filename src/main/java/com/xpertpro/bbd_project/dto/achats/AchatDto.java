package com.xpertpro.bbd_project.dto.achats;

import com.xpertpro.bbd_project.dto.items.ItemDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AchatDto {
    private Long id;
    private LocalDateTime createdAt;
    private String referenceVersement;
    private Double montantVerser;
    private Double montantRestant;
    private String fournisseur;
    private String fournisseurPhone;
    private String client;
    private String clientPhone;
    private List<ItemDto> items;
}
