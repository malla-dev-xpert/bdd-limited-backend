package com.xpertpro.bbd_project.dto.achats;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;

@Data
public class CreateAchatDto {
    @Column(nullable = true)
    private Long versementId;
    private List<CreateItemsDto> items;

}
