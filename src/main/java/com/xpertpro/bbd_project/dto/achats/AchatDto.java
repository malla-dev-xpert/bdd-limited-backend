package com.xpertpro.bbd_project.dto.achats;

import com.xpertpro.bbd_project.entity.LigneAchat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AchatDto {
    private Long id;
    private LocalDateTime createdAt;
    private String referenceVersement;
    private Double montantTotalVersement;
    private Double montantRestantVersement;
    private String partnerName;
    private String partnerPhone;
    private String partnerCountry;
    private List<LigneAchat> lignes;
}
