package com.xpertpro.bbd_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
public class ExpeditionDto {
    private Long id;
    private String expeditionType;
    private double weight;
    private double cbn;
    private String startCountry;
    private String ref;
    private String destinationCountry;
    private LocalDateTime arrivalDate;
    private LocalDateTime startDate;
    private Long clientId;
    private String clientName;
}
