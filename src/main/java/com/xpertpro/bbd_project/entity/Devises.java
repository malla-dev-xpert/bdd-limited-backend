package com.xpertpro.bbd_project.entity;

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
    private String name;
    private Double rate;
    @Column(unique = true)
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
