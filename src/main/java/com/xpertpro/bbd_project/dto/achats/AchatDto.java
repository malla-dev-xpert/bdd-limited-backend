package com.xpertpro.bbd_project.dto.achats;

import com.xpertpro.bbd_project.dto.items.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class AchatDto {
    private Long id;
    private LocalDateTime createdAt;
    private String referenceVersement;
    private Double montantTotal;
    private String client;
    private String clientPhone;
    private String status;
    private List<ItemDto> items;
}
