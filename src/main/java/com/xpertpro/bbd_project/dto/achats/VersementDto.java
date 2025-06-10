package com.xpertpro.bbd_project.dto.achats;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VersementDto {
    private Long id;
    private String reference;
    private Double montantVerser;
    private Double montantRestant;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private String partnerName;
    private String partnerPhone;
    private String partnerCountry;
    private String partnerAccountType;
    private Long partnerId;
    private Long deviseId;
    private String deviseCode;
    private List<AchatDto> achats;
    private String commissionnaireName;
    private String commissionnairePhone;
}
