package com.xpertpro.bbd_project.dto;

import lombok.Data;

@Data
public class CashWithdrawalDto {
    private Long id;
    private Long partnerId;
    private Long versementId;
    private Long deviseId;
    private Double montant;
    private String note;
    private Long userId;
}
