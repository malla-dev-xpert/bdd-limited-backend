package com.xpertpro.bbd_project.dto.containers;

import com.xpertpro.bbd_project.dto.PackageDto;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class ContainersDto {
    private Long id;
    private String reference;
    private String size;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();
    private Boolean isAvailable;
    private String status;
    private Long userId;
    private String userName;
    private Long harborId;
    private String harborName;
    private List<PackageDto> packages;
    private String supplierName;
    @JoinColumn(nullable = true)
    private Long supplier_id;
    private String supplierPhone;
    private Double locationFee;
    private Double  localCharge;
    private Double  loadingFee;
    private Double  overweightFee;
    private Double  checkingFee;
    private Double telxFee;
    private Double  otherFees;
    private Double  margin;
    private Double amount;
}
