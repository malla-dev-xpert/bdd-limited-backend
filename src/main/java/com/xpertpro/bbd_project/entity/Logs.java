package com.xpertpro.bbd_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "logs")
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // Ex: "CREATE_PACKAGE", "UPDATE_CONTAINER", etc.

    @Column(nullable = false)
    private String entityType; // Ex: "Packages", "Containers", etc.

    private Long entityId; // ID de l'entité concernée

    @Column(columnDefinition = "TEXT")
    private String details; // Détails supplémentaires au format JSON si nécessaire

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // Utilisateur qui a effectué l'action
}
