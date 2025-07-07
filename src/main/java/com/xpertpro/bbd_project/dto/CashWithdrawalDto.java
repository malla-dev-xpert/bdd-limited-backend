package com.xpertpro.bbd_project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CashWithdrawalDto {
    private Long id;
    private Long partnerId;
    private String userName;
    private Long versementId;
    private Long deviseId;
    private Double montant;
    private String note;
    private Long userId;
    private LocalDateTime dateRetrait;
}
