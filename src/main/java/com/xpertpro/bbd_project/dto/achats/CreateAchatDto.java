package com.xpertpro.bbd_project.dto.achats;

import lombok.Data;

import java.util.List;

@Data
public class CreateAchatDto {
    private Long versementId;
    private List<CreateLigneDto> lignes;
}
