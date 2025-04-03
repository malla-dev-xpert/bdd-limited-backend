package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Data @AllArgsConstructor @NoArgsConstructor
@Table(name = "devises")
public class Devises {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // Ex: Dollar, Euro
    private Double rate; // Le taux de change

    @Column(unique = true)
    private String code; // Ex: USD, EUR

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
