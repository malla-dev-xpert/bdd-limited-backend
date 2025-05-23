package com.xpertpro.bbd_project.dto.partners;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.dto.achats.VersementDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class PartnerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String country;
    private String adresse;
    private String accountType;
    private double balance;
    private List<VersementDto> versements;
    private List<ExpeditionDto> expeditions;
}
