package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "carrier_services")
public class CarrierServiceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;
    @ManyToOne
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carriers carriers;
}
